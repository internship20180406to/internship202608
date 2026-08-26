package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor //定型コード自動生成

public class InvestmentTrustForm {
    @NonNull//HTML状でrequiredを使用
//    金融機関名
    private String bankName;
    @NonNull
//    口座番号
    private Integer bankAccountNum;
//    購入者名
    @NonNull
    private String purchaserName;
//    銘柄
    @NonNull
    private String investmentTrustName;
//    科目名
    @NonNull
    private String bankSubject;
//    支店名
    @NonNull
    private String branch;
//    購入金額
    @NonNull
    private Integer purchaseAmount;

//    Integerを使用しているのは、未入力をnull状態で表現するため

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
