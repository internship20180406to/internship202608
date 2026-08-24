package com.example.internship.repository;

import com.example.internship.entity.BankTransferForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BankTransferRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void create(BankTransferForm bankTransferForm) {
        String sql = "INSERT INTO bankTransfer_table(bankName, branchName, bankAccountType, bankAccountNum, name, money, transferDate Time) VALUES(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, bankTransferForm.getBankName(), bankTransferForm.getBranchname(), bankTransferForm.getAccountname(),bankTransferForm.getBankAccountNum() ,bankTransferForm.getAccountholder() ,bankTransferForm.getAmount() ,bankTransferForm.getScheduledtransfer());
    }

}
