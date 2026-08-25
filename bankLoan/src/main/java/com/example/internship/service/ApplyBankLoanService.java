package com.example.internship.service;

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

    public void applyBankLoan(BankLoanForm bankLoanForm) {
        bankLoanRepository.create(bankLoanForm);
    }
}