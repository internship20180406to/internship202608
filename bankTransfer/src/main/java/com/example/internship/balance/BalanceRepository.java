package com.example.internship.balance;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// 口座残高の読み書き。利用者ごとに1行だけ持つ。
//
// 本来は口座開設のときに行ができるが、このアプリには口座開設が無い。
// 利用者を切り替えたときに残高が無いと何も試せないので、
// 初めて読むときに初期残高で作る。ログインを作るときに見直すこと
@Repository
public class BalanceRepository {

    // 口座開設の代わり。初めての利用者に与える残高
    static final int INITIAL_AMOUNT = 1_000_000;

    private final JdbcTemplate jdbcTemplate;

    public BalanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 今の残高。まだ無ければ初期残高で作ってから返す
    public int amountOf(String userId) {
        Integer amount = jdbcTemplate.query(
                "SELECT amount FROM balance WHERE userId = ?",
                rs -> rs.next() ? rs.getInt("amount") : null, userId);
        if (amount != null) {
            return amount;
        }
        // 同時に2つの画面から読まれても行は1つ。既にあれば作らない
        jdbcTemplate.update("""
                INSERT INTO balance (userId, amount) VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE amount = amount
                """, userId, INITIAL_AMOUNT);
        return amountOf(userId);
    }

    // 残高から引く。足りなければ引かずに false を返す。
    //
    // 「残高を読む → 足りるか確かめる → 引く」と3手に分けると、その隙間に
    // 別の振込が確定して二重に引ける。条件をUPDATEのWHEREに入れることで、
    // 確かめることと引くことが1手になる
    public boolean withdraw(String userId, int money) {
        String sql = "UPDATE balance SET amount = amount - ? WHERE userId = ? AND amount >= ?";
        return jdbcTemplate.update(sql, money, userId, money) == 1;
    }
}
