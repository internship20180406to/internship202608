package com.example.internship.controller;

import com.example.internship.entity.BankLoanForm;
import com.example.internship.service.ApplyBankLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class BankLoanController {

    @Autowired
    private ApplyBankLoanService applyBankLoanService;

    @GetMapping("/bankLoan")
    public String bankTransfer(Model model) {
        model.addAttribute("bankLoanApplication", new BankLoanForm());
        model.addAttribute("nameOptions", "山陰共同銀行");
        model.addAttribute("branchOptions", new String[]{"本店営業部", "福岡支店", "博多支店"});
        return "bankLoanMain";
    }

    @PostMapping("/bankLoanConfirmation")
    public String confirmation(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        bankLoanForm.setDebtorName(
                bankLoanForm.getDebtorLastName()
                        + " "
                        + bankLoanForm.getDebtorFirstName()
        );
        bankLoanForm.setBankName("ながれぼし銀行");
        model.addAttribute("bankName", bankLoanForm.getBankName());
        model.addAttribute("bankAccountNum", bankLoanForm.getBankAccountNum());
        model.addAttribute("bankLoanApplication", bankLoanForm);
        return "bankLoanConfirmation";
    }

    @PostMapping("/bankLoanCompletion")
    public String completion(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        applyBankLoanService.applyBankLoan(bankLoanForm);
        return "bankLoanCompletion";
    }

}
