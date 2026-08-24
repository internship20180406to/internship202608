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
//    金融機関名
    private String bankName;
    @NonNull
//    口座番号
    private Integer bankAccountNum;
//    購入者名
    private String purchaserName;
//    銘柄
    private String investmentTrustName;
//    科目名
    private String bankSubject;
//    支店名
    private String branch;
//    購入金額
    private Integer purchaseAmount;

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
}
