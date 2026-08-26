package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.constraints.Past;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankLoanForm {
    @NotBlank(message = "金融機関名を入力してください")
    private String bankName;
    @NotBlank(message = "支店名を入力してください")
    private String branchName;
    @NotBlank(message = "科目を選択してください")
    private String bankAccountType;
    @NotBlank(message = "口座番号を入力してください")
    @Pattern(regexp = "[0-9]{7}", message = "口座番号は7桁の数字で入力してください")
    private String bankAccountNum;
    @NotBlank(message = "姓を入力してください")
    private String lastName;

    @NotBlank(message = "名を入力してください")
    private String firstName;

    @NotBlank(message = "姓のフリガナを入力してください")
    @Pattern(
            regexp = "^[ァ-ヶー]+$",
            message = "姓のフリガナはカタカナで入力してください"
    )
    private String lastNameKana;

    @NotBlank(message = "名のフリガナを入力してください")
    @Pattern(
            regexp = "^[ァ-ヶー]+$",
            message = "名のフリガナはカタカナで入力してください"
    )
    private String firstNameKana;
    @NotNull(message = "借入金額を入力してください")
    @Min(value = 1, message = "借入金額は1万円以上で入力してください")
    @Max(value = 1000, message = "借入金額は1000万円以下で入力してください")
    private Integer loanAmount;
    @NotNull(message = "年収を入力してください")
    @Min(value = 1, message = "年収は1万円以上で入力してください")
    @Max(value = 100000, message = "年収が上限を超えています")
    private Integer annualIncome;

    @AssertTrue(message = "借入限度額は年収の50％(10万単位)が上限になります")
    public boolean isLoanAmountWithinLimit() {

        if (loanAmount == null || annualIncome == null) {
            return true;
        }

        int limit = (int) Math.floor((annualIncome * 0.5) / 10) * 10;

        return loanAmount <= limit;
    }

    @NotNull(message = "金利を入力してください")
    @DecimalMin("0.95")
    @DecimalMax("14.5")
    private Double interestRate;
    @NotNull(message = "生年月日を入力してください")
    @Past(message = "生年月日は過去の日付を入力してください")
    private LocalDate birthDate;
    @AssertTrue(message = "20歳未満の方はお申し込みいただけません")
    public boolean isAgeEligible() {

        if (birthDate == null) {
            return true;
        }

        int age = Period.between(
                birthDate,
                LocalDate.now()
        ).getYears();

        return age >= 20;
    }


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

    public String getLastNameKana() {
        return lastNameKana;
    }

    public void setLastNameKana(String lastNameKana) {
        this.lastNameKana = lastNameKana;
    }

    public String getFirstNameKana() {
        return firstNameKana;
    }

    public void setFirstNameKana(String firstNameKana) {
        this.firstNameKana = firstNameKana;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }
}
