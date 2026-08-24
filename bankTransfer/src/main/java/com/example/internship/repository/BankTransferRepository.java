package com.example.internship.repository;

import com.example.internship.entity.BankTransferForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
//SQLにデータの挿入を行うクラスを定義
@Repository
public class BankTransferRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void create(BankTransferForm bankTransferForm) {
        String sql = """
        INSERT INTO bankTransfer_table
            (bankName, branchName, bankAccountType, bankAccountNum,
             name, money, transferDateTime)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                bankTransferForm.getBankName(),
                bankTransferForm.getBranchName(),
                bankTransferForm.getBankAccountType(),
                bankTransferForm.getBankAccountNum(),
                bankTransferForm.getName(),
                bankTransferForm.getMoney(),
                bankTransferForm.getTransferDateTime());
    }

}
