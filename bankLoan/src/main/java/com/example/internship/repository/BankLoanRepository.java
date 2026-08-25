package com.example.internship.repository;

import com.example.internship.entity.BankLoanForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BankLoanRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void create(BankLoanForm bankLoanForm) {
        String sql = "INSERT INTO bankLoan_table(" +
                "bankName, branchName, bankAccountType, bankAccountNum, name, loanAmount, annualIncome, InterestRate)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                bankLoanForm.getBankName(),
                bankLoanForm.getBranchName(),
                bankLoanForm.getBankAccountType(),
                bankLoanForm.getBankAccountNum(),
                bankLoanForm.getName(),
                bankLoanForm.getLoanAmount() * 10000,
                bankLoanForm.getAnnualIncome() *10000,
                bankLoanForm.getInterestRate());
    }

}
