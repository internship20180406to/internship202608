package com.example.internship.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 自分の口座は1件のみ運用する前提
    public Integer findBalance() {
        String sql = "SELECT balance FROM account_table LIMIT 1";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public String findBankName() {
        String sql = "SELECT bankName FROM account_table LIMIT 1";
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    // 引き落とし口座選択画面に表示する自分の口座情報を取得する
    public MyAccount findMyAccount() {
        String sql = "SELECT bankName, accountType, accountNum, balance FROM account_table LIMIT 1";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new MyAccount(
                rs.getString("bankName"),
                rs.getString("accountType"),
                rs.getString("accountNum"),
                rs.getInt("balance")
        ));
    }

    // 残高が足りている場合のみ減算する。戻り値は更新された行数（0なら残高不足）
    public int decreaseBalance(int amount) {
        String sql = "UPDATE account_table SET balance = balance - ? WHERE id = 1 AND balance >= ?";
        return jdbcTemplate.update(sql, amount, amount);
    }

    public record MyAccount(String bankName, String accountType, String accountNum, int balance) {
    }
}
