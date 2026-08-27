package com.example.internship.service;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.repository.AccountBalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 申込の成立処理（引き落とし＋登録）のテスト。
 *
 * 画面を通さずサービスを直接呼ぶことで、
 * 「画面のチェックは通ったのに、引き落としの瞬間には残高が足りなかった」
 * という状況を再現している。
 * 実際の運用では、確認画面を見ている間に別の申込で残高が減るとこうなる。
 */
@SpringBootTest
@Transactional
class OrderInvestmentTrustServiceTest {

    //  残高15,000円の口座
    private static final String BANK = "0002";
    private static final String BRANCH = "012";
    private static final String TYPE = "普通";
    private static final String NUM = "9999999";

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private InvestmentTrustForm formOf(int money) {
        InvestmentTrustForm form = new InvestmentTrustForm();
        form.setBankCode(BANK);
        form.setBankName("こぶた銀行");
        form.setBranchCode(BRANCH);
        form.setBranchName("大手町支店");
        form.setBankAccountType(TYPE);
        form.setBankAccountNum(NUM);
        form.setName("ﾉｺﾘ ｽｸﾅｲ");
        form.setFundName("キャピタル１");
        form.setMoney(money);
        return form;
    }

    private int countOrders() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM investmenttrust_table", Integer.class);
    }

    @Test
    @DisplayName("申込が成立すると、引き落とし後の残高が返る")
    void 正常系() {
        int ordersBefore = countOrders();

        long balanceAfter = orderInvestmentTrustService.orderInvestmentTrust(formOf(10000));

        assertThat(balanceAfter).isEqualTo(5000L);
        assertThat(countOrders()).isEqualTo(ordersBefore + 1);
    }

    @Test
    @DisplayName("引き落とし直前に残高が減っていた場合、例外になり申込も登録されない")
    void 残高不足なら申込ごと成立しない() {
        //  確認画面を見ている間に、別の申込で残高が使われてしまった状況を作る
        accountBalanceRepository.withdraw(BANK, BRANCH, TYPE, NUM, 15000L);
        int ordersBefore = countOrders();

        assertThatThrownBy(() -> orderInvestmentTrustService.orderInvestmentTrust(formOf(10000)))
                .isInstanceOf(InsufficientBalanceException.class);

        //  ここが要点。引き落としを先に行っているので、成立しない申込は登録されない。
        //  「残高は減っていないのに申込だけ残る」という食い違いが起きない。
        assertThat(countOrders()).isEqualTo(ordersBefore);
        assertThat(accountBalanceRepository.find(BANK, BRANCH, TYPE, NUM).orElseThrow().getBalance())
                .isZero();
    }

    @Test
    @DisplayName("存在しない口座なら例外になり、申込は登録されない")
    void 存在しない口座() {
        InvestmentTrustForm form = formOf(10000);
        form.setBankAccountNum("0000000");
        int ordersBefore = countOrders();

        assertThatThrownBy(() -> orderInvestmentTrustService.orderInvestmentTrust(form))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(countOrders()).isEqualTo(ordersBefore);
    }
}
