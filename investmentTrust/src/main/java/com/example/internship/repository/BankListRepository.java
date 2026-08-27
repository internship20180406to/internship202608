package com.example.internship.repository;

import com.example.internship.entity.BankListForm;
import com.example.internship.entity.InvestmentTrustForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BankListRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void get(BankListForm bankListForm) {
        String sql =
                "SELECT * FROM bank_list WHERE bank_code = ? AND branch_code = ?";
        //ここの変数（名前）は触らないこと！変更なしでOK！

        jdbcTemplate.queryForObject(
                sql,
                String.class,
                bankListForm.getBankCode(),
                bankListForm.getBranchCode()
        );
    }
}
