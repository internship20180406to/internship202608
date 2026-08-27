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
    private String bankAccountNum;


    @NonNull
    private String name;

    private String lastName;
    private String firstName;


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

    public String getBranchName() {return branchName; }

    public void setBranchName(String branchName) {this.branchName = branchName; }

    public String getBankAccountType() {return bankAccountType;}

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(String bankAccountNum) {this.bankAccountNum = bankAccountNum;}

    public String getLastName() {return lastName;}

    public void setLastName(String lastName) {this.lastName = lastName;}

    public String getFirstName() {return firstName;}

    public void setFirstName(String firstName) {this.firstName = firstName;}

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public java.time.LocalDate getTransferDateTime() {return transferDateTime; }

    public void setTransferDateTime( java.time.LocalDate  transferDateTime) { this.transferDateTime = transferDateTime;
    }
}
