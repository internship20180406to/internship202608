package com.example.internship.service;

import com.example.internship.entity.AccountRegistrationForm;
import com.example.internship.repository.AccountBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 口座を登録するサービス。
 *
 * AccountBalanceService は @Transactional(readOnly = true) の参照専用なので、
 * 更新を行うこちらは別のクラスに分けている。
 */
@Service
@Transactional
public class RegisterAccountService {

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    /** 口座を1件登録する */
    public void registerAccount(AccountRegistrationForm form) {
        accountBalanceRepository.insert(form.getBankCode(), form.getBranchCode(),
                form.getAccountType(), form.getAccountNum(),
                form.getAccountName(), form.getBalance());
    }

    /** 同じ4点セットの口座が既に登録されているか */
    @Transactional(readOnly = true)
    public boolean exists(AccountRegistrationForm form) {
        return accountBalanceRepository.find(form.getBankCode(), form.getBranchCode(),
                form.getAccountType(), form.getAccountNum()).isPresent();
    }
}
