package com.example.internship.service;

import java.util.List;
import com.example.internship.entity.BankLoanForm;
import com.example.internship.repository.BankLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplyBankLoanService {

    @Autowired
    private BankLoanRepository bankLoanRepository;

    // データベースへの登録処理
    public void applyBankLoan(BankLoanForm bankLoanForm) {
        bankLoanRepository.create(bankLoanForm);
    }

    // ★ 登録済みデータをすべて取得する処理
    public List<BankLoanForm> getAllBankLoans() {
        return bankLoanRepository.findAll();
    }
}