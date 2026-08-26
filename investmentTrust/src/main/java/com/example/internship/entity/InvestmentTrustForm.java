package com.example.internship.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 投資信託の申込フォーム。
 * 各項目に付けたアノテーションがサーバサイドの入力チェック（Bean Validation）になる。
 * フロント側（inputConfirmation.js）にも同じ条件のチェックを実装しているが、
 * ブラウザの開発者ツールなどでJSを回避できるため、最終的な判定は必ずこちらで行う。
 */
@Data
public class InvestmentTrustForm {

    //  @NotBlank:String型に作用。null, 空文字, 空白のみをすべて拒否
    @NotBlank(message = "金融機関名を選択してください。")
    private String bankName;

    //  @NotNull:全型に作用。nullを拒否
    //  @Min/@Max:数値の範囲を判定。1000000〜9999999 とすることで「7桁ちょうど」を表現している
    @NotNull(message = "口座番号を入力してください。")
    @Min(value = 1000000, message = "口座番号は半角数字7桁で入力してください。")
    @Max(value = 9999999, message = "口座番号は半角数字7桁で入力してください。")
    private Integer bankAccountNum;

    @NotBlank(message = "支店名を選択してください。")
    private String branchName;

    @NotBlank(message = "科目名を選択してください。")
    private String bankAccountType;

    //  @Size:文字数の上限・下限を判定。DBの桁あふれを防ぐ
    @NotBlank(message = "購入者名を入力してください。")
    @Size(max = 30, message = "購入者名は30文字以内で入力してください。")
    private String name;

    @NotBlank(message = "銘柄を選択してください。")
    private String fundName;

    @NotNull(message = "金額を入力してください。")
    @Min(value = 10000, message = "金額は10,000円以上で入力してください。")
    @Max(value = 10000000, message = "金額は10,000,000円以下で入力してください。")
    private Integer money;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Integer getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(Integer bankAccountNum) {
        this.bankAccountNum = bankAccountNum;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }
}
