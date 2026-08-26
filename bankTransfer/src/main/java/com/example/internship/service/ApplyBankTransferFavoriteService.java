package com.example.internship.service;

import com.example.internship.entity.BankTransferFavoriteForm;
import com.example.internship.repository.BankTransferFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApplyBankTransferFavoriteService {
    @Autowired
    private BankTransferFavoriteRepository bankTransferFavoriteRepository;

    public void registerFavorite(BankTransferFavoriteForm form) {
        bankTransferFavoriteRepository.create(form);
    }

    public List<BankTransferFavoriteForm> getFavorites() {
        return bankTransferFavoriteRepository.findAll();
    }

    public void deleteFavorite(Integer id) {
        bankTransferFavoriteRepository.deleteById(id);
    }
}