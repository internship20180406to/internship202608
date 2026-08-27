package com.example.internship.service;

import com.example.internship.entity.BankListForm;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.repository.BankListRepository;
import com.example.internship.repository.InvestmentTrustRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankListService {
    @Autowired
    private BankListRepository bankListRepository;

    public List<BankListForm> getName(InvestmentTrustForm investmentTrustForm) {
    return bankListRepository.get(investmentTrustForm);
    }
}
