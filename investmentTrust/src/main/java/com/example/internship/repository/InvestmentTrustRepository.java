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
        String sql = "INSERT INTO investmentTrust_table(bankName, branchName, bankAccountType,bankAccountNum) VALUES(?, ?, ?, ?)";
        jdbcTemplate.update(sql, investmentTrustForm.getBankName(), investmentTrustForm.getBranchName(), investmentTrustForm.getBankAccountTypeName(), investmentTrustForm.getBankAccountNum());
    }

}
