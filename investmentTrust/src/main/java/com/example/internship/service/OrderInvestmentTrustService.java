package com.example.internship.service;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.repository.AccountBalanceRepository;
import com.example.internship.repository.InvestmentTrustRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 投資信託の申込を成立させるサービス。
 *
 * @Transactional が付いているので、このメソッドの中で行う
 * 「残高の引き落とし」と「申込の登録」は1つのトランザクションになる。
 * 途中で例外が起きればどちらも無かったことになるので、
 * 「残高だけ減って申込が残っていない」「申込はあるのに残高が減っていない」
 * という食い違いが起きない。
 */
@Service
@Transactional
public class OrderInvestmentTrustService {

    @Autowired
    private InvestmentTrustRepository investmentTrustRepository;

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    /**
     * 残高を引き落として申込を登録し、引き落とし後の残高を返す。
     *
     * ★引き落としを先に行っているのがポイント。
     *   先に申込を登録してしまうと、そのあとで残高不足が分かった場合に
     *   「巻き戻し」に頼ることになる。引き落としを先にすれば、
     *   成立しない申込はそもそも登録されない。
     *
     * @throws InsufficientBalanceException 残高不足、または口座が存在しない場合
     */
    public long orderInvestmentTrust(InvestmentTrustForm form) {
        int withdrawn = accountBalanceRepository.withdraw(
                form.getBankCode(), form.getBranchCode(),
                form.getBankAccountType(), form.getBankAccountNum(), form.getMoney());

        if (withdrawn == 0) {
            //  画面で残高を確認してから「申込」を押すまでの間に、
            //  別の申込で残高が減っていた場合もここに来る。
            throw new InsufficientBalanceException("残高が不足しているか、口座が存在しません。");
        }

        investmentTrustRepository.create(form);

        //  同じトランザクションの中なので、今引き落とした結果が読める
        return accountBalanceRepository.find(form.getBankCode(), form.getBranchCode(),
                        form.getBankAccountType(), form.getBankAccountNum())
                .orElseThrow(() -> new IllegalStateException("引き落とした口座が見つかりません。"))
                .getBalance();
    }
}
