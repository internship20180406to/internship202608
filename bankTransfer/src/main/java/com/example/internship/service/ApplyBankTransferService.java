package com.example.internship.service;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.repository.AccountRepository;
import com.example.internship.repository.BankTransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ApplyBankTransferService {
    @Autowired
    private BankTransferRepository bankTransferRepository;
    @Autowired
    private AccountRepository accountRepository;

    public void applyBankTransfer(BankTransferForm bankTransferForm) {
        int updatedRows = accountRepository.decreaseBalance(bankTransferForm.getMoney());
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("口座残高が不足しています");
        }
        bankTransferRepository.create(bankTransferForm);
    }
    public List<BankTransferForm> getRecentTransfers() {
        return bankTransferRepository.findRecentTransfers();
    }
    public Integer getBalance() {
        return accountRepository.findBalance();
    }
}
