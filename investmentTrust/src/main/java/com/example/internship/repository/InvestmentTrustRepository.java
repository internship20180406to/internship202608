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
        String sql =
        "INSERT INTO investmentTrust_table(bankName, branchName, bankAccountType, bankAccountNum, name, fundCode, money) VALUES(?, ?, ?, ?, ?, ?, ?)";

        String fullName = investmentTrustForm.getLastName() + investmentTrustForm.getFirstName();

        int investedMoney = 0;

        if (investmentTrustForm.getAmount() != null) {
            investedMoney = investmentTrustForm.getAmount();
        }

        else {
            investedMoney = investmentTrustForm.getPurchaseAmount();
        }

        jdbcTemplate.update(
                sql,
                investmentTrustForm.getBankCode(),
                investmentTrustForm.getBranchName(),
                investmentTrustForm.getAccountType(),
                investmentTrustForm.getBankAccountNum(),
                fullName,
                investmentTrustForm.getFundCode(),
                investmentTrustForm.getMoney()
        );
    }
}
