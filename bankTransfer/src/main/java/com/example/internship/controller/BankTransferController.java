package com.example.internship.controller;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.service.ApplyBankTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class BankTransferController {

    @Autowired
    private ApplyBankTransferService applyBankTransferService;

    @GetMapping("/bankTransfer")
    public String bankTransfer(Model model) {
        model.addAttribute("bankTransferApplication", new BankTransferForm());
        model.addAttribute("nameOptions", "山陰共同銀行");
        return "bankTransferMain";
    }

    @PostMapping("/bankTransferConfirmation")
    public String confirmation(@ModelAttribute BankTransferForm bankTransferForm, Model model) {
        bankTransferForm.setBankName("ながれぼし銀行");
        model.addAttribute("bankName", bankTransferForm.getBankName());
        model.addAttribute("bankAccountNum", bankTransferForm.getBankAccountNum());
        model.addAttribute("bankTransferApplication", bankTransferForm);
        return "bankTransferConfirmation";
    }

    @PostMapping("/bankTransferCompletion")
    public String completion(@ModelAttribute BankTransferForm bankTransferForm, Model model) {
        applyBankTransferService.applyBankTransfer(bankTransferForm);
        return "bankTransferCompletion";
    }

}
