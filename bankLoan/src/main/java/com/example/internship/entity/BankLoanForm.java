package com.example.internship.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BankLoanForm {

    private String bankName;         // 金融機関名
    private String branchName;       // 支店名
    private String bankAccountType;  // 預金種別
    private String accountType;      // ローン種類
    private Integer bankAccountNum;  // 口座番号
    private String name;             // 債務者名
    private String nameKana;         // フリガナ
    private String birthDate;        // 生年月日
    private String phoneNumber;      // 電話番号
    private String email;            // メールアドレス
    private String postalCode;       // 郵便番号
    private String address;          // 住所
    private Integer loanAmount;      // 借入金額
    private Integer annualIncome;    // 借入年収
    private Double interestRate;     // 金利
    private String loanYears;        // ローン年数（返済期間）

    private String repaymentMethod;  // 返済方法

    // 💡 以下の3つのフィールドを追加してください
    private String interestType;     // 金利タイプ
    private String bonusOption;      // ボーナス併用払い（利用する/利用しない）
    private Integer bonusAmount;     // ボーナス月追加返済額

}