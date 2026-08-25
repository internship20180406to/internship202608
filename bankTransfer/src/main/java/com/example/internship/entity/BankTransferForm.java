package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

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
    @NonNull
    private Integer money;
    @NonNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transferDateTime;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
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

    public String getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(String bankAccountNum) {
        if (bankAccountNum != null && bankAccountNum.matches("[0-9]{1,7}")) {
            this.bankAccountNum = "0".repeat(7 - bankAccountNum.length()) + bankAccountNum;
        } else {
            this.bankAccountNum = bankAccountNum;
        }
    }

    //振込画面側
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public LocalDate getTransferDateTime() {
        return transferDateTime;
    }

    public void setTransferDateTime(LocalDate transferDateTime) {
        this.transferDateTime = transferDateTime;
    }


}
