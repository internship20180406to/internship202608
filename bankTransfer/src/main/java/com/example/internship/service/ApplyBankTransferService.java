package com.example.internship.service;

import com.example.internship.balance.BalanceRepository;
import com.example.internship.entity.BankTransferInput;
import com.example.internship.repository.BankTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//入力内容をデータベースに反映させるように指示する
@Service
@Transactional
public class ApplyBankTransferService {

    private final BankTransferRepository bankTransferRepository;
    private final BalanceRepository balanceRepository;

    public ApplyBankTransferService(BankTransferRepository bankTransferRepository,
                                    BalanceRepository balanceRepository) {
        this.bankTransferRepository = bankTransferRepository;
        this.balanceRepository = balanceRepository;
    }

    // 残高を引いてから記録する。引けなければ何も起きず false を返す。
    //
    // 先に引くのは、足りるかの確認と引く操作が withdraw の中で1手になっているため。
    // @Transactional が付いているので、記録の途中で失敗すれば残高も戻る
    public boolean applyBankTransfer(String userId, BankTransferInput input) {
        // 引かれるのは振込額と手数料の合計
        if (!balanceRepository.withdraw(userId, input.getTotal())) {
            return false;
        }
        bankTransferRepository.create(userId, input);
        return true;
    }
}
