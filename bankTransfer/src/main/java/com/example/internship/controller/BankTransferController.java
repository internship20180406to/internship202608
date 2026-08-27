package com.example.internship.controller;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.entity.TransferRecord;
import com.example.internship.service.ApplyBankTransferService;
import com.example.internship.service.InsufficientBalanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;


@Controller

    public class BankTransferController {

    @Autowired
    private ApplyBankTransferService applyBankTransferService;

    // セッションにデータがないときだけ、新しいフォームを作る
    @ModelAttribute("bankTransferApplication")
    public BankTransferForm createBankTransferForm() {
        return new BankTransferForm();
    }

    @GetMapping("/bankTransfer")
    public String bankTransfer(
            @ModelAttribute("bankTransferApplication") BankTransferForm bankTransferForm,
            @RequestParam(name = "startStep", required = false) Integer startStep,
            Model model) {
        addBankTransferMainAttributes(model);
        model.addAttribute("startStep", startStep);
        return "bankTransferMain";
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public String handleInsufficientBalance(Model model) {
        model.addAttribute("bankTransferApplication", new BankTransferForm());
        addBankTransferMainAttributes(model);
        model.addAttribute("errorMessage", "口座残高が不足しているため振込できませんでした。");
        return "bankTransferMain";
    }

    private void addBankTransferMainAttributes(Model model) {
        model.addAttribute("nameOptionsBankName", applyBankTransferService.getBankNames());
        model.addAttribute("branchOptions", applyBankTransferService.getBranchOptions());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("recentTransfers", applyBankTransferService.getRecentTransfers());
        model.addAttribute("favoriteTransfers", applyBankTransferService.getFavorites());
        model.addAttribute("balance", applyBankTransferService.getBalance());
        model.addAttribute("todayAvailable", applyBankTransferService.getTodayAvailableAmount());
        model.addAttribute("myBankName", applyBankTransferService.getMyBankName());
        model.addAttribute("myAccount", applyBankTransferService.getMyAccount());
    }

    @PostMapping("/bankTransferConfirmation")
    public String confirmation(@ModelAttribute("bankTransferApplication") BankTransferForm bankTransferForm, Model model){
        model.addAttribute("bankName", bankTransferForm.getBankName());
        model.addAttribute("branchName", bankTransferForm.getBranchName());
        model.addAttribute("subjectName", bankTransferForm.getBankAccountType());
        model.addAttribute("bankAccountNum", bankTransferForm.getBankAccountNum());

        model.addAttribute("name", bankTransferForm.getName());
        model.addAttribute("money", bankTransferForm.getMoney());
        model.addAttribute("transferDateTime", bankTransferForm.getTransferDateTime());

        int fee = applyBankTransferService.calculateFee(bankTransferForm.getBankName(), bankTransferForm.getMoney());
        model.addAttribute("fee", fee);
        model.addAttribute("totalDebit", bankTransferForm.getMoney() + fee);
        model.addAttribute("requiresReconfirmation", applyBankTransferService.requiresReconfirmation(bankTransferForm));

        model.addAttribute("bankTransferApplication", bankTransferForm);

        return "bankTransferConfirmation";
    }

    @PostMapping("/bankTransferCompletion")
    public String completion(
            @ModelAttribute("bankTransferApplication")
            BankTransferForm bankTransferForm,
            @RequestParam(name = "registerFavorite", defaultValue = "false")
            boolean registerFavorite,
            Model model) {

        ApplyBankTransferService.TransferResult result = applyBankTransferService.applyBankTransfer(bankTransferForm, registerFavorite);

        model.addAttribute("bankName", bankTransferForm.getBankName());
        model.addAttribute("branchName", bankTransferForm.getBranchName());
        model.addAttribute("bankAccountType", bankTransferForm.getBankAccountType());
        model.addAttribute("bankAccountNum", bankTransferForm.getBankAccountNum());
        model.addAttribute("name", bankTransferForm.getName());
        model.addAttribute("money", bankTransferForm.getMoney());
        model.addAttribute("fee", result.fee());
        model.addAttribute("totalDebit", result.totalDebit());
        model.addAttribute("transferDateTime", bankTransferForm.getTransferDateTime());
        model.addAttribute("isCompleted", result.isCompleted());

        return "bankTransferCompletion";
    }

    @GetMapping("/bankTransferStatus")
    public String status(Model model) {
        model.addAttribute("transfers", applyBankTransferService.getTransferHistory());
        return "bankTransferStatus";
    }

    @GetMapping("/bankTransferCancelConfirmation")
    public String cancelConfirmation(@RequestParam int id, Model model) {
        TransferRecord transfer = applyBankTransferService.getTransferById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定された振込が見つかりません: " + id));
        model.addAttribute("transfer", transfer);
        return "bankTransferCancelConfirmation";
    }

    @PostMapping("/bankTransferCancel")
    public String cancel(@RequestParam int id, Model model) {
        boolean cancelled = applyBankTransferService.cancelTransfer(id);
        model.addAttribute("cancelMessage", cancelled
                ? "振込を取消しました。"
                : "この振込は取消できませんでした（処理待ちでない、または取消期限〔振込指定日の午前6時〕を過ぎています）。");
        model.addAttribute("transfers", applyBankTransferService.getTransferHistory());
        return "bankTransferStatus";
    }

}
