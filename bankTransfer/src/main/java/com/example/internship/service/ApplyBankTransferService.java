package com.example.internship.service;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.entity.TransferRecord;
import com.example.internship.entity.TransferStatus;
import com.example.internship.repository.AccountRepository;
import com.example.internship.repository.BankRepository;
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
    private static final int RECONFIRMATION_AMOUNT_THRESHOLD = 100_000;

    @Autowired
    private BankTransferRepository bankTransferRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private BankRepository bankRepository;

    public TransferResult applyBankTransfer(BankTransferForm bankTransferForm, boolean registerFavorite) {
        int fee = calculateFee(bankTransferForm.getBankName(), bankTransferForm.getMoney());
        boolean isToday = bankTransferForm.getTransferDateTime().isEqual(LocalDate.now());
        if (isToday) {
            int updatedRows = accountRepository.decreaseBalance(bankTransferForm.getMoney() + fee);
            if (updatedRows == 0) {
                throw new InsufficientBalanceException("口座残高が不足しています");
            }
        }
        // 未来日の場合は予約振込として記録するだけで、残高はまだ減らさない
        TransferStatus status = isToday ? TransferStatus.COMPLETED : TransferStatus.PENDING;
        bankTransferRepository.create(bankTransferForm, fee, status);
        if (registerFavorite) {
            favoriteRepository.create(bankTransferForm);
        }
        return new TransferResult(status, fee, bankTransferForm.getMoney() + fee);
    }

    public record TransferResult(TransferStatus status, int fee, int totalDebit) {
        public boolean isCompleted() {
            return status == TransferStatus.COMPLETED;
        }
    }

    // 振込手数料を計算する：振込先が自分と同じ銀行なら0円、異なる銀行なら3万円未満220円・3万円以上440円
    public int calculateFee(String recipientBankName, int money) {
        String myBankName = accountRepository.findBankName();
        if (myBankName.equals(recipientBankName)) {
            return 0;
        }
        return money >= 30_000 ? 440 : 220;
    }

    // 振込先が「初めての振込先」かどうかを判定する（CANCELLEDのみの履歴は初めて扱い）
    public boolean isFirstTimeRecipient(BankTransferForm bankTransferForm) {
        return !bankTransferRepository.hasPriorTransferTo(
                bankTransferForm.getBankName(),
                bankTransferForm.getBranchName(),
                bankTransferForm.getBankAccountType(),
                bankTransferForm.getBankAccountNum());
    }

    // 再確認モーダル表示要否を判定する（高額 or 初めての振込先）
    public boolean requiresReconfirmation(BankTransferForm bankTransferForm) {
        boolean isHighAmount = bankTransferForm.getMoney() != null && bankTransferForm.getMoney() >= RECONFIRMATION_AMOUNT_THRESHOLD;
        return isHighAmount || isFirstTimeRecipient(bankTransferForm);
    }

    // 指定日を迎えた予約振込の残高を減算する（バッチ処理から呼ばれる）
    public void processDueReservedTransfers() {
        LocalDate today = LocalDate.now();
        for (PendingTransfer pending : bankTransferRepository.findDueUnprocessedTransfers(today)) {
            int updatedRows = accountRepository.decreaseBalance(pending.money() + pending.fee());
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

    public String getMyBankName() {
        return accountRepository.findBankName();
    }

    public AccountRepository.MyAccount getMyAccount() {
        return accountRepository.findMyAccount();
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

    public List<String> getBankNames() {
        return bankRepository.findAllBankNames();
    }

    public List<BankRepository.BranchOption> getBranchOptions() {
        return bankRepository.findAllBranchOptions();
    }
}
