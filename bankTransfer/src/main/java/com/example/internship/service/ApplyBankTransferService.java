package com.example.internship.service;

import com.example.internship.entity.BankTransferForm;
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

    public void applyBankTransfer(BankTransferForm bankTransferForm) {
        bankTransferRepository.create(bankTransferForm);
    }
    public List<BankTransferForm> getRecentTransfers() {
        return bankTransferRepository.findRecentTransfers();
    }
}
