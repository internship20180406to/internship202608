package com.example.internship.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "金融機関名を選択してください")//必須チェックを追加
    private String bankName;          // 金融機関名

    @NotBlank(message = "支店名を入力してください")
    private String branchName;        // 支店名

    @NotBlank(message = "科目を選択してください")
    private String bankAccountType;   // 科目

    @NotBlank(message = "口座番号を入力してください")
    private String bankAccountNum;    // 口座番号（型をInteger=>Stringに変更）

    @NotBlank(message = "口座名義を入力してください")
    private String name;              // 口座名義

    @NotNull(message = "金額を入力してください")
    private Integer money;            // 金額

    @NotNull(message = "振込指定日を入力してください")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transferDateTime;   // 振込指定日
}