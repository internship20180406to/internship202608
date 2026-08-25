package com.example.internship.repository;

import java.util.List;
import com.example.internship.entity.BankLoanForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BankLoanRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. データの登録処理
    public void create(BankLoanForm bankLoanForm) {
        // ★ name_kana を nameKana に変更
        String sql = "INSERT INTO bankLoan_table (" +
                "bankName, branchName, bankAccountType, bankAccountNum, name, nameKana, accountType, loanAmount, annualIncome, interestRate" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                bankLoanForm.getBankName(),
                bankLoanForm.getBranchName(),
                bankLoanForm.getBankAccountType(),
                bankLoanForm.getBankAccountNum(),
                bankLoanForm.getName(),
                bankLoanForm.getNameKana(),
                bankLoanForm.getAccountType(),
                bankLoanForm.getLoanAmount(),
                bankLoanForm.getAnnualIncome(),
                bankLoanForm.getInterestRate()
        );
    }

    // 2. 全件取得処理
    public List<BankLoanForm> findAll() {
        String sql = "SELECT * FROM bankLoan_table";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BankLoanForm.class));
    }
}