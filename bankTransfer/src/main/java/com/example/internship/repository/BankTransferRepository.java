package com.example.internship.repository;

import com.example.internship.entity.BankTransferForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class BankTransferRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void create(BankTransferForm bankTransferForm) {
        String sql = "INSERT INTO bankTransfer_table(bankName, branchName, bankAccountType, bankAccountNum,name,money,transferDateTime) VALUES(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, bankTransferForm.getBankName(), bankTransferForm.getBranchName(), bankTransferForm.getBankAccountType(), bankTransferForm.getBankAccountNum(), bankTransferForm.getName(), bankTransferForm.getMoney(), bankTransferForm.getTransferDateTime());
    }
    // 直近の振込先を最大3件取得する
    public List<BankTransferForm> findRecentTransfers() {
        String sql =
                "SELECT bankName, branchName, bankAccountType, " + "bankAccountNum, name " + "FROM bankTransfer_table " + "ORDER BY id DESC " + "LIMIT 3";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BankTransferForm form = new BankTransferForm();
            form.setBankName(rs.getString("bankName"));
            form.setBranchName(rs.getString("branchName"));
            form.setBankAccountType(rs.getString("bankAccountType"));
            form.setBankAccountNum(rs.getString("bankAccountNum"));
            form.setName(rs.getString("name"));

            return form;
        });
    }

}
