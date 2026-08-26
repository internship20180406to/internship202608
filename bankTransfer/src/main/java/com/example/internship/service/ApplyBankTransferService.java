package com.example.internship.service;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.entity.TransferRecord;
import com.example.internship.entity.TransferStatus;
import com.example.internship.repository.AccountRepository;
import com.example.internship.repository.BankTransferRepository;
import com.example.internship.repository.BankTransferRepository.PendingTransfer;
import com.example.internship.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApplyBankTransferService {
    @Autowired
    private BankTransferRepository bankTransferRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;

    public void applyBankTransfer(BankTransferForm bankTransferForm, boolean registerFavorite) {
        boolean isToday = bankTransferForm.getTransferDateTime().isEqual(LocalDate.now());
        if (isToday) {
            int updatedRows = accountRepository.decreaseBalance(bankTransferForm.getMoney());
            if (updatedRows == 0) {
                throw new InsufficientBalanceException("口座残高が不足しています");
            }
        }
        // 未来日の場合は予約振込として記録するだけで、残高はまだ減らさない
        bankTransferRepository.create(bankTransferForm, isToday ? TransferStatus.COMPLETED : TransferStatus.PENDING);
        if (registerFavorite) {
            favoriteRepository.create(bankTransferForm);
        }
    }

    // 指定日を迎えた予約振込の残高を減算する（バッチ処理から呼ばれる）
    public void processDueReservedTransfers() {
        LocalDate today = LocalDate.now();
        for (PendingTransfer pending : bankTransferRepository.findDueUnprocessedTransfers(today)) {
            int updatedRows = accountRepository.decreaseBalance(pending.money());
            if (updatedRows > 0) {
                bankTransferRepository.markCompleted(pending.id());
            }
            // 残高不足の場合は処理せずスキップし、翌日以降のバッチで再試行する
        }
    }

    // 振込内容確認画面向けの一覧を取得する
    public List<TransferRecord> getTransferHistory() {
        return bankTransferRepository.findAllOrderByDateDesc();
    }

    public Optional<TransferRecord> getTransferById(int id) {
        return bankTransferRepository.findById(id);
    }

    // 取消を実行する。取消できた場合はtrue、条件を満たさず取消できなかった場合はfalseを返す
    public boolean cancelTransfer(int id) {
        return bankTransferRepository.cancel(id) > 0;
    }

    public List<BankTransferForm> getRecentTransfers() {
        return bankTransferRepository.findRecentTransfers();
    }
    public Integer getBalance() {
        return accountRepository.findBalance();
    }

    // 本日の振込可能額を計算する（1日の上限は5,000,000円）
    public Integer getTodayAvailableAmount() {
        final int DAILY_LIMIT = 5_000_000;
        int usedToday = bankTransferRepository.sumTransferredOn(LocalDate.now());
        int balance = accountRepository.findBalance();
        int remainingLimit = Math.max(0, DAILY_LIMIT - usedToday);
        return Math.min(balance, remainingLimit);
    }

    public List<BankTransferForm> getFavorites() {
        return favoriteRepository.findAll();
    }
}
