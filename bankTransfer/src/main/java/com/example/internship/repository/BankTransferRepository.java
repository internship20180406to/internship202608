package com.example.internship.repository;

import com.example.internship.entity.BankTransferInput;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
//SQLにデータの挿入を行うクラスを定義
@Repository
public class BankTransferRepository {

    private final JdbcTemplate jdbcTemplate;

    public BankTransferRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 誰の振込かを記録する。userId は入力内容ではないので
    // BankTransferInput には入れず、別の引数として受け取る
    public void create(String userId, BankTransferInput input) {
        String sql = """
        INSERT INTO bankTransfer_table
            (userId, bankCode, bankName, branchCode, branchName, bankAccountType,
             bankAccountNum, name, money, fee, transferDateTime)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                userId,
                input.getBankCode(),
                input.getBankName(),
                input.getBranchCode(),
                input.getBranchName(),
                input.getBankAccountType(),
                input.getBankAccountNum(),
                input.getName(),
                input.getMoney(),
                input.getFee(),
                input.getTransferDateTime());
    }

}
