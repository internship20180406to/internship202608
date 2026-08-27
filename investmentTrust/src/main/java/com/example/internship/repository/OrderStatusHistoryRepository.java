package com.example.internship.repository;

import com.example.internship.entity.OrderStatusHistoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

// 注文ステータスの変更履歴(order_status_history)の登録・取得を行うRepository
@Repository
public class OrderStatusHistoryRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // ステータス変更を1件記録する(いつ・誰が処理したか)
    public void insert(Long orderId, String status, String changedBy) {
        String sql = "INSERT INTO order_status_history (order_id, status, changed_by) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, orderId, status, changedBy);
    }

    // 指定した注文の変更履歴を古い順に取得する(顧客向けステータス確認画面のタイムライン表示用)
    public List<OrderStatusHistoryEntry> findByOrderId(Long orderId) {
        String sql = "SELECT id, order_id, status, changed_by, changed_at FROM order_status_history " +
                "WHERE order_id = ? ORDER BY changed_at ASC, id ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new OrderStatusHistoryEntry(
                rs.getLong("id"),
                rs.getLong("order_id"),
                rs.getString("status"),
                rs.getString("changed_by"),
                rs.getTimestamp("changed_at").toLocalDateTime()
        ), orderId);
    }
}
