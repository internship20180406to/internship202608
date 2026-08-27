package com.example.internship.service;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.repository.InvestmentTrustRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderInvestmentTrustService {

    @Autowired
    private InvestmentTrustRepository investmentTrustRepository;

    public void orderInvestmentTrust(
            InvestmentTrustForm investmentTrustForm) {

        investmentTrustForm.setApplicationDate(
                LocalDateTime.now()
        );

        investmentTrustForm.setStatus("確認中");

        investmentTrustForm.setPurchaseDate(null);

        investmentTrustRepository.create(
                investmentTrustForm
        );
    }

    public List<InvestmentTrustForm> getHistory() {

        return investmentTrustRepository.findAll();
    }
}