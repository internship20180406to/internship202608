package com.example.internship.repository;

import com.example.internship.entity.AccountBalance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 残高の引き落としのテスト。
 *
 * ★テストに @Transactional を付けると、各テストの最後に自動でロールバックされる。
 *   残高を減らすテストを何度実行しても、DBの状態は元のまま保たれる。
 *
 * ★接続先は src/test/resources/application.yml で internship_test に切り替えてある。
 *   開発用の internship には触らないので、画面で確認しているデータには影響しない。
 */
@SpringBootTest
@Transactional
class AccountBalanceRepositoryTest {

    //  03_balance_data.sql で用意した残高15,000円の口座
    private static final String BANK = "0002";
    private static final String BRANCH = "012";
    private static final String TYPE = "普通";
    private static final String NUM = "9999999";

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    private long balanceOf(String bank, String branch, String type, String num) {
        return accountBalanceRepository.find(bank, branch, type, num)
                .map(AccountBalance::getBalance)
                .orElseThrow();
    }

    @Test
    @DisplayName("口座を4点セットで引ける")
    void 口座の検索() {
        AccountBalance account = accountBalanceRepository.find(BANK, BRANCH, TYPE, NUM).orElseThrow();

        assertThat(account.getAccountName()).isEqualTo("ﾉｺﾘ ｽｸﾅｲ");
        assertThat(account.getBalance()).isEqualTo(15000L);
    }

    @Test
    @DisplayName("口座番号が同じでも、銀行や支店が違えば別の口座として扱われる")
    void 口座は4点セットで特定される() {
        //  銀行だけ変えると見つからない
        assertThat(accountBalanceRepository.find("0001", BRANCH, TYPE, NUM)).isEmpty();
        //  科目だけ変えても見つからない
        assertThat(accountBalanceRepository.find(BANK, BRANCH, "貯蓄", NUM)).isEmpty();
    }

    @Test
    @DisplayName("同じ口座番号が別の銀行にも存在し、それぞれ別の口座として引ける")
    void 同じ口座番号でも銀行が違えば別口座() {
        //  初期データで 1234567 を 0001/002 と 0002/001 の両方に用意してある
        AccountBalance yamada = accountBalanceRepository.find("0001", "002", "普通", "1234567").orElseThrow();
        AccountBalance kobuta = accountBalanceRepository.find("0002", "001", "普通", "1234567").orElseThrow();

        //  口座番号だけで検索していたら、どちらか一方しか取れないか、
        //  そもそも「どちらの口座か」を決められない
        assertThat(yamada.getAccountName()).isEqualTo("ﾔﾏﾀﾞ ﾀﾛｳ");
        assertThat(yamada.getBalance()).isEqualTo(5000000L);
        assertThat(kobuta.getAccountName()).isEqualTo("ｺﾌﾞﾀ ｼﾞﾛｳ");
        assertThat(kobuta.getBalance()).isEqualTo(2000000L);
    }

    @Test
    @DisplayName("引き落としも4点セットで効くので、同じ口座番号の別口座には影響しない")
    void 引き落としは対象の口座だけ() {
        accountBalanceRepository.withdraw("0001", "002", "普通", "1234567", 1000000L);

        assertThat(balanceOf("0001", "002", "普通", "1234567")).isEqualTo(4000000L);
        //  同じ口座番号のもう一方は減っていない
        assertThat(balanceOf("0002", "001", "普通", "1234567")).isEqualTo(2000000L);
    }

    @Test
    @DisplayName("残高が足りていれば引き落とせる")
    void 引き落とし成功() {
        int updated = accountBalanceRepository.withdraw(BANK, BRANCH, TYPE, NUM, 10000L);

        assertThat(updated).isEqualTo(1);
        assertThat(balanceOf(BANK, BRANCH, TYPE, NUM)).isEqualTo(5000L);
    }

    @Test
    @DisplayName("ちょうど残高と同額なら引き落とせて、残高は0になる")
    void 全額引き落とし() {
        int updated = accountBalanceRepository.withdraw(BANK, BRANCH, TYPE, NUM, 15000L);

        assertThat(updated).isEqualTo(1);
        assertThat(balanceOf(BANK, BRANCH, TYPE, NUM)).isZero();
    }

    @Test
    @DisplayName("残高が1円でも足りなければ、更新件数0で残高は変わらない")
    void 残高不足では引き落とせない() {
        int updated = accountBalanceRepository.withdraw(BANK, BRANCH, TYPE, NUM, 15001L);

        //  ここが要点。UPDATE文のWHERE句に「残高が足りること」を含めているので、
        //  足りなければ1件も更新されず、残高はそのまま。マイナスにはならない。
        assertThat(updated).isZero();
        assertThat(balanceOf(BANK, BRANCH, TYPE, NUM)).isEqualTo(15000L);
    }

    @Test
    @DisplayName("存在しない口座を引き落とそうとしても、更新件数0で終わる")
    void 存在しない口座() {
        int updated = accountBalanceRepository.withdraw("9999", "999", TYPE, "0000000", 1000L);

        assertThat(updated).isZero();
    }

    @Test
    @DisplayName("続けて引き落とすと、2回目は残高不足で止まる")
    void 連続した引き落とし() {
        assertThat(accountBalanceRepository.withdraw(BANK, BRANCH, TYPE, NUM, 10000L)).isEqualTo(1);
        //  残りは5,000円しかないので、もう一度10,000円は引き落とせない
        assertThat(accountBalanceRepository.withdraw(BANK, BRANCH, TYPE, NUM, 10000L)).isZero();
        assertThat(balanceOf(BANK, BRANCH, TYPE, NUM)).isEqualTo(5000L);
    }
}
