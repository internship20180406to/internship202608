package com.example.internship.repository;

import com.example.internship.entity.InvestmentTrustForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InvestmentTrustRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void create(InvestmentTrustForm investmentTrustForm) {
        String sql = "INSERT INTO investmentTrust_table(" +
                "bankName, bankAccountNum, branchName, bankAccountType, " +
                "name, fundName, money) " +
                "VALUES(?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, investmentTrustForm.getBankName(), investmentTrustForm.getBankAccountNum(), investmentTrustForm.getBranch(),
                investmentTrustForm.getBankSubject(), investmentTrustForm.getPurchaserName(), investmentTrustForm.getInvestmentTrustName(),
                investmentTrustForm.getPurchaseAmount());
    }

}
