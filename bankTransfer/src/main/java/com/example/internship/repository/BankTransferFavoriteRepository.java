package com.example.internship.repository;

import com.example.internship.entity.BankTransferFavoriteForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BankTransferFavoriteRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void create(BankTransferFavoriteForm form) {
        String sql = "INSERT INTO bankTransfer_favorite_table(bankName, branchName, bankAccountType, bankAccountNum, name) VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                form.getBankName(),
                form.getBranchName(),
                form.getBankAccountType(),
                form.getBankAccountNum(),
                form.getName()
        );
    }

    public List<BankTransferFavoriteForm> findAll() {
        String sql = "SELECT bankName, branchName, bankAccountType, bankAccountNum, name FROM bankTransfer_favorite_table";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BankTransferFavoriteForm form = new BankTransferFavoriteForm();

            form.setBankName(rs.getString("bankName"));
            form.setBranchName(rs.getString("branchName"));
            form.setBankAccountType(rs.getString("bankAccountType"));
            form.setBankAccountNum(rs.getString("bankAccountNum"));
            form.setName(rs.getString("name"));

            return form;
        });
    }
}