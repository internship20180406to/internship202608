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
        return new RecentPayee(
                rs.getString("bankCode"),
                rs.getString("bankName"),
                rs.getString("branchCode"),
                rs.getString("branchName"),
                rs.getString("bankAccountType"),
                rs.getString("bankAccountNum"),
                rs.getString("name"),
                last == null ? null : last.toLocalDate());
    };

    // 画面に出す件数の上限。履歴は増え続けるので、必ず区切る
    private static final int LIMIT = 20;

    // 同じ振込先を1件にまとめる。
    // 名義や金融機関名は後から変わることがあるので、まとめた中の
    // 最も新しい行の値を採る（MAX(id) の行）。
    private static final String SELECT = """
            SELECT t.bankCode, t.branchCode, t.bankAccountType, t.bankAccountNum,
                   latest.bankName, latest.branchName, latest.name,
                   t.lastTransferredOn
              FROM (
                    SELECT bankCode, branchCode, bankAccountType, bankAccountNum,
                           MAX(id) AS latestId,
                           MAX(transferDateTime) AS lastTransferredOn
                      FROM bankTransfer_table
                     WHERE userId = ?
                       AND bankCode IS NOT NULL
                     GROUP BY bankCode, branchCode, bankAccountType, bankAccountNum
                   ) t
              JOIN bankTransfer_table latest ON latest.id = t.latestId
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
        String sql = SELECT + " ORDER BY t.lastTransferredOn DESC, t.latestId DESC LIMIT " + LIMIT;
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    // 画面から選ばれた振込先が、その利用者の履歴に実在するかの確認に使う。
    // 送られてきた値をそのまま信じず、必ずここで引き直す
    public Optional<RecentPayee> find(String userId, String bankCode, String branchCode,
                                      String bankAccountType, String bankAccountNum) {
        String sql = SELECT + """
                 WHERE t.bankCode = ? AND t.branchCode = ?
                   AND t.bankAccountType = ? AND t.bankAccountNum = ?
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, userId, bankCode, branchCode,
                bankAccountType, bankAccountNum).stream().findFirst();
    }
}
