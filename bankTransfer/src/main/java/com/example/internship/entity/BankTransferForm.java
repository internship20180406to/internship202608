package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
//入力内容を格納するクラスを定義
public class BankTransferForm {

    private String bankName;          // 金融機関名
    private String branchName;        // 支店名
    private String bankAccountType;   // 科目
    private String bankAccountNum;    // 口座番号（型をInteger=>Stringに変更）
    private String name;              // 口座名義
    private Integer money;            // 金額

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)//これがget/setの役割を担う
    private LocalDate transferDateTime;   // 振込指定日
}

