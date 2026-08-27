package com.example.internship.service;

import com.example.internship.entity.BankListForm;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.repository.BankListRepository;
import com.example.internship.repository.InvestmentTrustRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class BankListService {
    @Autowired
    private BankListRepository bankListRepository;

    public void getName(BankListForm bankListForm) {
        bankListRepository.get(bankListForm);
    }
}
