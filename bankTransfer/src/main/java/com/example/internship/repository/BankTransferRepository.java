package com.example.internship.repository;

import com.example.internship.entity.BankTransferInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
//SQLにデータの挿入を行うクラスを定義
@Repository
public class BankTransferRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void create(BankTransferInput input) {
        String sql = """
        INSERT INTO bankTransfer_table
            (bankCode, bankName, branchCode, branchName, bankAccountType,
             bankAccountNum, name, money, transferDateTime)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                input.getBankCode(),
                input.getBankName(),
                input.getBranchCode(),
                input.getBranchName(),
                input.getBankAccountType(),
                input.getBankAccountNum(),
                input.getName(),
                input.getMoney(),
                input.getTransferDateTime());
    }

}
