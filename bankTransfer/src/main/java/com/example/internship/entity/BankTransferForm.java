package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankTransferForm {
    @NonNull
    private String bankName;
    @NonNull
    private String branchName;
    @NonNull
    private String bankAccountType;
    @NonNull
    private Integer bankAccountNum;
    @NonNull
    private String name;
    @NonNull
    private Integer money;
    @NonNull
    private java.time.LocalDate transferDateTime;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchname() {return branchName; }

    public void setBranchname(String branchname) {this.branchName = branchName; }

    public String getAccountname() {
        return Accountname;
    }

    public void setAccountname(String Accountname) {
        this.Accountname = Accountname;
    }

    public Integer getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(Integer bankAccountNum) {
        this.bankAccountNum = bankAccountNum;
    }

    public String getAccountholder() {
        return Accountholder;
    }

    public void setAccountholder(String Accountholder) {
        this.Accountholder = Accountholder;
    }

    public Integer getAmount() {
        return Amount;
    }

    public void setAmount(Integer Amount) {
        this.Amount = Amount;
    }

    public java.time.LocalDate getScheduledtransfer() {return Scheduledtransfer; }

    public void setScheduledtransfer( java.time.LocalDate  Scheduledtransfer) { this.Scheduledtransfer = Scheduledtransfer;
    }
}
