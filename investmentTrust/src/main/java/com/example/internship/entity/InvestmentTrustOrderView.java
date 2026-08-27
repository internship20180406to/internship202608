package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 行員モードの注文一覧表示用(マスタとJOINして名称解決済みの読み取り専用データ)
@Data
@AllArgsConstructor
public class InvestmentTrustOrderView {
    private Long id;
    private Long customerId;
    private String status;
    private String institutionName;
    private String branchName;
    private String bankAccountNum;
    private String bankSubject;
    private String lastName;
    private String firstName;
    private String address;
    private String contact;
    private String fundCode;
    private String fundName;
    private Integer purchaseAmount;
    private Integer purchaseFee;
    private LocalDateTime orderDatetime;
    private LocalDate tradeDate;
    // 申込時点の概算口数(常に保持)
    private Long estimatedUnits;
    // 約定処理で確定した口数(未約定の間はnull)
    private Long confirmedUnits;
}
