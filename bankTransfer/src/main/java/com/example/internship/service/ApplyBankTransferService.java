package com.example.internship.service;

import com.example.internship.entity.BankTransferInput;
import com.example.internship.repository.BankTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//入力内容をデータベースに反映させるように指示する
@Service
@Transactional
public class ApplyBankTransferService {

    private final BankTransferRepository bankTransferRepository;

    public ApplyBankTransferService(BankTransferRepository bankTransferRepository) {
        this.bankTransferRepository = bankTransferRepository;
    }

    public void applyBankTransfer(String userId, BankTransferInput input) {
        bankTransferRepository.create(userId, input);
    }
}
