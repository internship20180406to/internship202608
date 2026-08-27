package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
// 注文ステータスの変更履歴(order_status_history)の1行を表すEntity。いつ・誰が処理したかを保持する
public class OrderStatusHistoryEntry {
    private Long id;
    private Long orderId;
    private String status;
    private String changedBy;
    private LocalDateTime changedAt;
}
