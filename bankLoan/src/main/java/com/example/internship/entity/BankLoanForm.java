package com.example.internship.entity;

public class BankLoanForm {

    private String bankName;         // 金融機関名
    private String branchName;       // 支店名
    private String bankAccountType;  // 預金種別（depositType ➔ bankAccountType）
    private String accountType;      // ローン種類
    private Integer bankAccountNum;  // 口座番号
    private String name;             // 債務者名（debtorName ➔ name）
    private Integer loanAmount;      // 借入金額（borrowingAmount ➔ loanAmount）
    private Integer annualIncome;    // 借入年収
    private Double interestRate;     // 金利

    // --- Getter / Setter ---

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

    public String getAccountType() {
        return accountType;
    }
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

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

    public Integer getLoanAmount() {
        return loanAmount;
    }
    public void setLoanAmount(Integer loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Integer getAnnualIncome() {
        return annualIncome;
    }
    public void setAnnualIncome(Integer annualIncome) {
        this.annualIncome = annualIncome;
    }

    public Double getInterestRate() {
        return interestRate;
    }
    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }
}