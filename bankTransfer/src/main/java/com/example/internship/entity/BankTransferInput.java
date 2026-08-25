package com.example.internship.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

// 画面をまたいで溜めていく入力内容。セッションに預けて持ち回る。
// ここには検証用のアノテーションを付けない。形の検証は画面ごとのフォーム
// （AccountForm / AmountForm）が担い、実在するかの確認はマスタ照合で行う。
// この器に入るのは、いずれかを通った後の値だけになる。
@Data
public class BankTransferInput implements Serializable {

    private String bankCode;          // 金融機関コード
    private String bankName;          // 金融機関名

    private String branchCode;        // 支店コード
    private String branchName;        // 支店名

    private String bankAccountType;   // 科目
    private String bankAccountNum;    // 口座番号
    private String name;              // 口座名義

    private Integer money;            // 金額

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transferDateTime;   // 振込指定日

    // ---- 各画面まで到達しているかの判定。直接URLを叩かれたときの差し戻しに使う ----
    // その画面で埋まる項目を漏れなく見る。1つだけ代表して見ると、
    // 「必ずまとめて代入される」という他所の約束に依存することになり、
    // 別の経路が増えたときに欠けたまま素通りしてしまう

    public boolean hasBank() {
        return bankCode != null && bankName != null;
    }

    public boolean hasBranch() {
        return hasBank() && branchCode != null && branchName != null;
    }

    public boolean hasAccount() {
        return hasBranch() && bankAccountType != null && bankAccountNum != null && name != null;
    }

    public boolean hasAmount() {
        return hasAccount() && money != null && transferDateTime != null;
    }
}
