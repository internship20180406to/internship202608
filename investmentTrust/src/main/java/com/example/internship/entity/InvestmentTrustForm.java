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
    private String BranchName;//支店名
    @NonNull
    private String SubjectName;//科目名
    @NonNull
    private Integer bankAccountNum;//口座番号

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    /*public String getBranchName() { return branchName; }

    public void setBranchName(String branchName) { this.branchName = branchName; }*/

    public Integer getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(Integer bankAccountNum) {
        this.bankAccountNum = bankAccountNum;
    }
}
