package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// 行員モードの顧客一覧表示用(申し込み件数・最終申し込み日時を集計済みのデータ)
@Data
@AllArgsConstructor
public class CustomerSummary {
    private Long customerId;
    private String lastName;
    private String firstName;
    private String address;
    private String contact;
    private Integer orderCount;
    private LocalDateTime lastOrderDatetime;
}
