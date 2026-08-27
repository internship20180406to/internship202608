package com.example.internship.entity;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankCustomerAccount {

    private Integer accountId;

    private Integer customerId;

    private String customerNumber;

    private String branchName;

    private String accountType;

    private String accountNumber;

    private String lastName;

    private String firstName;

    private String lastNameKana;

    private String firstNameKana;

    private LocalDate birthDate;
}