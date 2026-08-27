package com.example.internship.repository;

import com.example.internship.entity.AccountBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 口座残高の参照と引き落とし。
 */
@Repository
public class AccountBalanceRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final RowMapper<AccountBalance> ROW_MAPPER = (rs, rowNum) -> {
        AccountBalance balance = new AccountBalance();
        balance.setBankCode(rs.getString("bankCode"));
        balance.setBranchCode(rs.getString("branchCode"));
        balance.setAccountType(rs.getString("accountType"));
        balance.setAccountNum(rs.getString("accountNum"));
        balance.setAccountName(rs.getString("accountName"));
        balance.setBalance(rs.getLong("balance"));
        return balance;
    };

    /**
     * 口座を1件検索する。該当が無ければ空のOptional。
     * 「口座が実在するか」の確認と、残高の表示に使う。
     */
    public Optional<AccountBalance> find(String bankCode, String branchCode,
                                         String accountType, String accountNum) {
        String sql = "SELECT bankCode, branchCode, accountType, accountNum, accountName, balance"
                + " FROM account_balance"
                + " WHERE bankCode = ? AND branchCode = ? AND accountType = ? AND accountNum = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, bankCode, branchCode, accountType, accountNum)
                .stream().findFirst();
    }

    /**
     * 口座から指定額を引き落とす。実際に引き落とせた件数（0か1）を返す。
     *
     * ★「残高が足りるか」の判定をUPDATE文のWHERE句に含めているのが要点。
     *
     *   もし次のように2回に分けて書くと、
     *       1. SELECT で残高を読む
     *       2. 足りていれば UPDATE で減らす
     *   1と2の間に別の処理が同じ口座を引き落とすと、両方が「足りている」と判断して
     *   二重に引き落とされ、残高がマイナスになりうる（チェックしてから使うまでの隙間の問題）。
     *
     *   1文にまとめると、判定と更新がDBの中で一度に行われるのでこの隙間が生まれない。
     *   残高が足りなければWHERE句に一致せず、更新件数が0になる。
     *
     * ★戻り値が0になる理由は2つある。
     *     ・残高が足りない
     *     ・そもそもその口座が存在しない
     *   呼び出し側はどちらも「引き落とせなかった」として同じ扱いにしている。
     *
     * ※updatedAt は列定義に ON UPDATE CURRENT_TIMESTAMP を付けてあるので、
     *   ここで指定しなくても自動で更新される。
     */
    public int withdraw(String bankCode, String branchCode, String accountType,
                        String accountNum, long amount) {
        String sql = "UPDATE account_balance SET balance = balance - ?"
                + " WHERE bankCode = ? AND branchCode = ? AND accountType = ? AND accountNum = ?"
                + "   AND balance >= ?";
        return jdbcTemplate.update(sql, amount, bankCode, branchCode, accountType, accountNum, amount);
    }

    /**
     * 口座を新規に登録する。
     *
     * 同じ4点セットの口座が既にあると、主キー重複で DuplicateKeyException が投げられる。
     * 呼び出し側で「登録前に存在チェック」もしているが、
     * チェックから登録までの隙間に別の登録が入る可能性があるので、
     * 最終的にはDBの主キーが重複を防いでいる。
     *
     * updatedAt は列定義の DEFAULT CURRENT_TIMESTAMP で自動的に入る。
     */
    public void insert(String bankCode, String branchCode, String accountType,
                       String accountNum, String accountName, long balance) {
        String sql = "INSERT INTO account_balance"
                + "(bankCode, branchCode, accountType, accountNum, accountName, balance)"
                + " VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, bankCode, branchCode, accountType, accountNum, accountName, balance);
    }
}
