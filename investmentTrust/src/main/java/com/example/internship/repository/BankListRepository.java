package com.example.internship.repository;

import com.example.internship.entity.BankListForm;
import com.example.internship.entity.InvestmentTrustForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BankListRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<BankListForm> get(InvestmentTrustForm investmentTrustForm) {
        String sql =
                "SELECT * FROM bank_list WHERE bank_code = ? AND branch_code = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BankListForm form = new BankListForm();
            form.setBankCode(rs.getString("bank_code"));
            form.setBankName(rs.getString("bank_name"));
            form.setBranchCode(rs.getString("branch_code"));
            form.setBranchName(rs.getString("branch_name"));
            return form;
        }, investmentTrustForm.getBankCode(), investmentTrustForm.getBranchName());
    }
}
