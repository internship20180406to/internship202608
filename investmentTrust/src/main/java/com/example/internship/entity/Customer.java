package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// 顧客マスタ(customer_master)の1行を表すEntity
@Data
@AllArgsConstructor
public class Customer {
    private Long customerId;
    private String lastName;
    private String firstName;
    private String address;
    private String contact;
//    お客様モード(注文履歴確認)のログイン用パスワード(BCryptハッシュを保持。画面には表示しない)
    private String password;
    private LocalDateTime createdAt;
}
