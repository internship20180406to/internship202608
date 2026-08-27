package com.example.internship.service;

import com.example.internship.entity.AccountBalance;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.repository.AccountBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 口座残高の参照サービス。
 *
 * 引き落とし（更新）は申込の登録と同じトランザクションで行う必要があるため、
 * OrderInvestmentTrustService の中で扱っている。こちらは参照専用。
 */
@Service
@Transactional(readOnly = true)
public class AccountBalanceService {

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    /**
     * 申込フォームの内容から口座を1件検索する。
     * 口座は4点セットで特定するので、フォームから4つまとめて取り出している。
     */
    public Optional<AccountBalance> findByForm(InvestmentTrustForm form) {
        return accountBalanceRepository.find(form.getBankCode(), form.getBranchCode(),
                form.getBankAccountType(), form.getBankAccountNum());
    }
}
