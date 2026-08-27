package com.example.internship.entity;

public class BankListForm {
    
    private String bankCode;
    
    private String bankName;

    private String branchCode;

    private String branchName;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {return bankName;}

    public void setBankName(String bankName) {this.bankName = bankName;}
    
    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {return branchName;}

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
