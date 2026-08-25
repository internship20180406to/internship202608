package com.example.internship.entity;

public class BankLoanForm {

    private String bankName;         // 金融機関名
    private String branchName;       // 支店名
    private String depositType;      // 預金種別（普通預金・当座預金など）
    private String accountType;      // ローン種類（住宅ローン・マイカーローンなど）
    private Integer bankAccountNum;  // 口座番号
    private String debtorName;       // 債務者名
    private Integer borrowingAmount; // 借入金額
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

    public String getDepositType() {
        return depositType;
    }
    public void setDepositType(String depositType) {
        this.depositType = depositType;
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

    public String getDebtorName() {
        return debtorName;
    }
    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public Integer getBorrowingAmount() {
        return borrowingAmount;
    }
    public void setBorrowingAmount(Integer borrowingAmount) {
        this.borrowingAmount = borrowingAmount;
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