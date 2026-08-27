package com.example.internship.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 口座登録画面の入力フォーム。
 *
 * 申込画面（InvestmentTrustForm）と同じく、アノテーションがサーバ側の入力チェックになる。
 * フロント側（accountRegistration.js）にも同じ条件を実装しているが、
 * JSは開発者ツールで無効化できるため、最終的な判定は必ずこちらで行う。
 *
 * 金融機関名・支店名を持たないのも申込フォームと同じ理由。
 * 画面には表示するが送信はせず、サーバがコードからマスタを引き直す。
 */
@Data
public class AccountRegistrationForm {

    @NotBlank(message = "金融機関コードを入力してください。")
    @Pattern(regexp = "^[0-9]{4}$", message = "金融機関コードは半角数字4桁で入力してください。")
    private String bankCode;

    @NotBlank(message = "支店コードを入力してください。")
    @Pattern(regexp = "^[0-9]{3}$", message = "支店コードは半角数字3桁で入力してください。")
    private String branchCode;

    @NotBlank(message = "科目名を選択してください。")
    private String accountType;

    @NotBlank(message = "口座番号を入力してください。")
    @Pattern(regexp = "^[0-9]{7}$", message = "口座番号は半角数字7桁で入力してください。")
    private String accountNum;

    //  名義の条件は申込画面の購入者名と同じ（半角カナ＋半角スペース、20文字以内）。
    //  account_balance.accountName が varchar(20) なので上限も揃えている。
    @NotBlank(message = "口座名義を入力してください。")
    @Size(max = 20, message = "口座名義は20文字以内で入力してください。")
    @Pattern(regexp = "^[\uFF66-\uFF9F\u0020]+$",
            message = "口座名義は半角カナ（半角スペース可）で入力してください。")
    private String accountName;

    //  残高はintではなくLongで受ける。
    //  DBがBIGINTなので、intの上限（約21億）に引きずられないようにしている。
    //  上限を100億円にしているのは、桁を打ち間違えたときに気づけるようにするため。
    @NotNull(message = "初期残高を入力してください。")
    @Min(value = 0, message = "初期残高は0円以上で入力してください。")
    @Max(value = 10000000000L, message = "初期残高は10,000,000,000円以下で入力してください。")
    private Long balance;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNum() {
        return accountNum;
    }

    public void setAccountNum(String accountNum) {
        this.accountNum = accountNum;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
