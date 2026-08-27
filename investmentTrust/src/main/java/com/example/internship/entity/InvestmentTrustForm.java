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
    private String bankCode;
    @NonNull
    private String branchName;

    private String accountType;

    private Integer bankAccountNum;

    private String lastName;

    private String firstName;

    private String fundCode;

    private Integer amount;

    private Integer purchaseAmount;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBranchName() {return branchName;}

    public void setBranchName(String Name) {this.branchName = Name;}


    public String getAccountType() {
        return accountType;
    }

    public void setBankName(String accountType) {
        this.accountType = accountType;
    }

    public Integer getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(Integer bankAccountNum) {
        this.bankAccountNum = bankAccountNum;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFundCode() {return fundCode;}

    public void setFundCode(String fundCode) {this.fundCode = fundCode;}

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(Integer purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    public Integer getMoney() {
        if (this.amount != null && this.amount > 0) {
            return this.amount;
        }
        return this.purchaseAmount;
    }
}