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

        String sql =
                "INSERT INTO bankLoan_table(" +
                        "customerId, " +
                        "accountId, " +
                        "bankName, " +
                        "branchName, " +
                        "bankAccountType, " +
                        "bankAccountNum, " +
                        "name, " +
                        "birthDate, " +
                        "loanAmount, " +
                        "loanYears, " +
                        "annualIncome, " +
                        "interestType, " +
                        "interestRate" +
                        ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                bankLoanForm.getCustomerId(),
                bankLoanForm.getAccountId(),
                bankLoanForm.getBankName(),
                bankLoanForm.getBranchName(),
                bankLoanForm.getSubjectName(),
                bankLoanForm.getBankAccountNum(),
                bankLoanForm.getDebtorName(),
                bankLoanForm.getBirthDate(),
                bankLoanForm.getDesiredLoanAmount(),
                bankLoanForm.getLoanYears(),
                bankLoanForm.getAnnualIncome(),
                bankLoanForm.getInterestType(),
                bankLoanForm.getInterestRate()
        );
    }
}