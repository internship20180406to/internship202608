package com.example.internship.controller;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.service.ApplyBankTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

import java.util.List;


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
    public String bankTransfer(Model model) {
        model.addAttribute("nameOptionsBankName", List.of( "山陰共同銀行", "ながれぼし銀行", "青空銀行"));
        model.addAttribute("nameOptionsBranchName",  List.of("山陰共同支店", "本店", "中央支店"));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("recentTransfers", applyBankTransferService.getRecentTransfers());
        return "bankTransferMain";
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

        model.addAttribute("bankTransferApplication", bankTransferForm);

        return "bankTransferConfirmation";
    }

    @PostMapping("/bankTransferCompletion")
    public String completion(
            @ModelAttribute("bankTransferApplication")
            BankTransferForm bankTransferForm) {

        applyBankTransferService.applyBankTransfer(bankTransferForm);

        return "bankTransferCompletion";
    }



}
