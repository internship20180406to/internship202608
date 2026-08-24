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
        model.addAttribute("nameOptions", "山陰共同銀行");//金融機関名義
        model.addAttribute("branchName", "本店営業部");//支店名
        model.addAttribute("accountType", "普通預金");//科目名
        return "bankLoanMain";
    }


    @PostMapping("/bankLoanConfirmation")
    public String confirmation(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        bankLoanForm.setBankName("ながれぼし銀行");//口座名義
        model.addAttribute("bankName", bankLoanForm.getBankName()); //銀行名
        model.addAttribute("bankAccountNum", bankLoanForm.getBankAccountNum());//口座番号
        model.addAttribute("amount", bankLoanForm.getAmount()); //金額
        model.addAttribute("transferDate", bankLoanForm.getTransferDate()); //振込指定日
        model.addAttribute("bankLoanApplication", bankLoanForm);//銀行ローン申し込み

        return "bankLoanConfirmation";
    }


    @PostMapping("/bankLoanCompletion")
    public String completion(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        applyBankLoanService.applyBankLoan(bankLoanForm);
        return "bankLoanCompletion";
    }

}
