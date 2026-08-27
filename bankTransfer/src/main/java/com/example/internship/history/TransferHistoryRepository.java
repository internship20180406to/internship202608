package com.example.internship.history;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

// 過去の振込から「振込先」を拾い出す。申し込み記録は増える一方なので参照だけ。
//
// どのメソッドも必ず userId を受け取る。引数を減らすために省けるようにすると、
// うっかり全利用者の履歴を返す実装が書けてしまう。
@Repository
public class TransferHistoryRepository {

    private static final RowMapper<RecentPayee> ROW_MAPPER = (rs, rowNum) -> {
        Date last = rs.getDate("lastTransferredOn");
        // 金額は NULL を区別したいので、読んだ直後に wasNull() を見る。
        // getObject のキャストだと、列の型が変わったときに ClassCastException になる
        int amount = rs.getInt("lastAmount");
        Integer lastAmount = rs.wasNull() ? null : amount;
        return new RecentPayee(
                rs.getString("bankCode"),
                rs.getString("bankName"),
                rs.getString("branchCode"),
                rs.getString("branchName"),
                rs.getString("bankAccountType"),
                rs.getString("bankAccountNum"),
                rs.getString("name"),
                lastAmount,
                last == null ? null : last.toLocalDate());
    };

    // 画面に出す件数の上限。履歴は増え続けるので、必ず区切る
    private static final int LIMIT = 20;

    // 同じ振込先を1件にまとめる。
    // 名義や金融機関名は後から変わることがあるので、まとめた中の
    // 最も新しい行の値を採る。
    //
    // 集約関数（MAX）で列ごとに拾うと、項目が別々の行から集まった1件ができる。
    // 実際それで「12月1日に振り込んだ 1,000円」のような、日付と金額が
    // 噛み合わない表示が出た。行を1つ選び、その行から全部の値を採る。
    //
    // 「最も新しい行」は振込指定日で決める。画面に出しているのがその日付で、
    // 申込順（id）で選ぶと、先の日付を指定できるぶん表示とずれる。
    // 同じ日が並んだときだけ、後から申し込んだ方を新しいとみなす
    private static final String SELECT = """
            SELECT t.bankCode, t.bankName, t.branchCode, t.branchName,
                   t.bankAccountType, t.bankAccountNum, t.name,
                   t.money AS lastAmount,
                   t.transferDateTime AS lastTransferredOn
              FROM (
                    SELECT id, bankCode, bankName, branchCode, branchName,
                           bankAccountType, bankAccountNum, name, money, transferDateTime,
                           ROW_NUMBER() OVER (
                               PARTITION BY bankCode, branchCode,
                                            bankAccountType, bankAccountNum
                               ORDER BY transferDateTime DESC, id DESC) AS rn
                      FROM bankTransfer_table
                     WHERE userId = ?
                       AND bankCode IS NOT NULL
                   ) t
             WHERE t.rn = 1
            """;

    private final JdbcTemplate jdbcTemplate;

    public TransferHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // その利用者が過去に振り込んだ相手を、新しい順に返す。
    // 並べるのは画面に出している「最終振込日」そのもの。申込の順（id）で並べると、
    // 振込指定日は先の日付を選べるため、表示された日付が降順に見えなくなる。
    // 同じ日が並んだときだけ、後から申し込んだ方を先にする
    public List<RecentPayee> findRecent(String userId) {
        String sql = SELECT + " ORDER BY t.transferDateTime DESC, t.id DESC LIMIT " + LIMIT;
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    // 画面から選ばれた振込先が、その利用者の履歴に実在するかの確認に使う。
    // 送られてきた値をそのまま信じず、必ずここで引き直す
    public Optional<RecentPayee> find(String userId, String bankCode, String branchCode,
                                      String bankAccountType, String bankAccountNum) {
        String sql = SELECT + """
                   AND t.bankCode = ? AND t.branchCode = ?
                   AND t.bankAccountType = ? AND t.bankAccountNum = ?
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, userId, bankCode, branchCode,
                bankAccountType, bankAccountNum).stream().findFirst();
    }
}
