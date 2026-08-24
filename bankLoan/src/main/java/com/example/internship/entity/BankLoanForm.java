package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankLoanForm {
    @NonNull
    private String bankName;

    private String branchName;

    private String debtorName;
    private String debtorLastName;
    private String debtorFirstName;
    @NonNull
    private Integer bankAccountNum;

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
