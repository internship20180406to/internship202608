package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentTrustForm {
    @NonNull
    private String bankName;//金融機関名
    @NonNull
    private String branchName;//支店名
    @NonNull
    private String bankAccountTypeName;//科目名
    @NonNull
    private Integer bankAccountNum;//口座番号
    @NonNull
    private String name;//購入者名
    @NonNull
    private String fundName;//銘柄選択
    @NonNull
    private Integer money;//購入金額

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchName() { return branchName; }

    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getBankAccountTypeName() { return bankAccountTypeName; }

    public void setBankAccountTypeName(String bankAccountTypeName) { this.bankAccountTypeName = bankAccountTypeName; }

    public Integer getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(Integer bankAccountNum) {
        this.bankAccountNum = bankAccountNum;
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

    public Integer getMoney() { return money; }

    public void setMoney(Integer money) {
        this.money = money;
    }
}
