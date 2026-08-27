package com.example.internship.service;

import java.time.LocalDate;
import java.util.Optional;

import com.example.internship.entity.BankCustomerAccount;
import com.example.internship.repository.BankCustomerAccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankAccountVerificationService {

    @Autowired
    private BankCustomerAccountRepository
            bankCustomerAccountRepository;

    public Optional<BankCustomerAccount> verifyAccount(
            String branchName,
            String accountType,
            String accountNumber,
            LocalDate birthDate) {

        if (
                branchName == null
                        || accountType == null
                        || accountNumber == null
                        || birthDate == null
        ) {
            return Optional.empty();
        }

        return bankCustomerAccountRepository.findActiveAccount(
                branchName,
                accountType,
                accountNumber,
                birthDate
        );
    }
}