package com.example.internship.repository;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.entity.TransferRecord;
import com.example.internship.entity.TransferStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class BankTransferRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final RowMapper<TransferRecord> TRANSFER_RECORD_ROW_MAPPER = (rs, rowNum) -> new TransferRecord(
            rs.getInt("id"),
            rs.getString("bankName"),
            rs.getString("branchName"),
            rs.getString("bankAccountType"),
            rs.getString("bankAccountNum"),
            rs.getString("name"),
            rs.getInt("money"),
            rs.getDate("transferDateTime").toLocalDate(),
            TransferStatus.valueOf(rs.getString("status"))
    );

    public void create(BankTransferForm bankTransferForm, TransferStatus status) {
        String sql = "INSERT INTO bankTransfer_table(bankName, branchName, bankAccountType, bankAccountNum,name,money,transferDateTime,status) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, bankTransferForm.getBankName(), bankTransferForm.getBranchName(), bankTransferForm.getBankAccountType(), bankTransferForm.getBankAccountNum(), bankTransferForm.getName(), bankTransferForm.getMoney(), bankTransferForm.getTransferDateTime(), status.name());
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

    // 指定日に実行された（完了した）振込の合計金額を取得する
    public Integer sumTransferredOn(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(money), 0) AS total FROM bankTransfer_table WHERE transferDateTime = ? AND status = 'COMPLETED'";
        return jdbcTemplate.queryForObject(sql, Integer.class, date);
    }

    // 指定日を迎えたのにまだ残高が減算されていない予約振込を取得する
    public List<PendingTransfer> findDueUnprocessedTransfers(LocalDate today) {
        String sql = "SELECT id, money FROM bankTransfer_table WHERE status = 'PENDING' AND transferDateTime <= ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new PendingTransfer(rs.getInt("id"), rs.getInt("money")), today);
    }

    public void markCompleted(int id) {
        jdbcTemplate.update("UPDATE bankTransfer_table SET status = 'COMPLETED' WHERE id = ?", id);
    }

    // 振込内容確認画面用の一覧を振込指定日の新しい順で取得する
    public List<TransferRecord> findAllOrderByDateDesc() {
        String sql = "SELECT id, bankName, branchName, bankAccountType, bankAccountNum, name, money, transferDateTime, status "
                + "FROM bankTransfer_table ORDER BY transferDateTime DESC, id DESC";
        return jdbcTemplate.query(sql, TRANSFER_RECORD_ROW_MAPPER);
    }

    public Optional<TransferRecord> findById(int id) {
        String sql = "SELECT id, bankName, branchName, bankAccountType, bankAccountNum, name, money, transferDateTime, status "
                + "FROM bankTransfer_table WHERE id = ?";
        return jdbcTemplate.query(sql, TRANSFER_RECORD_ROW_MAPPER, id).stream().findFirst();
    }

    // 処理待ちかつ振込指定日の午前6時より前の場合のみ取消可能。戻り値は更新行数（0なら取消不可）
    public int cancel(int id) {
        String sql = "UPDATE bankTransfer_table SET status = 'CANCELLED' "
                + "WHERE id = ? AND status = 'PENDING' AND NOW() < DATE_ADD(transferDateTime, INTERVAL 6 HOUR)";
        return jdbcTemplate.update(sql, id);
    }

    public record PendingTransfer(int id, int money) {
    }
}
