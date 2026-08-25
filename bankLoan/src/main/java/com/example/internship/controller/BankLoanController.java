package com.example.internship.controller;

import java.util.List;

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

    // 1. 入力画面の表示
    @GetMapping("/bankLoan")
    public String bankTransfer(Model model) {
        model.addAttribute("bankLoanApplication", new BankLoanForm());
        model.addAttribute("nameOptions", "山陰共同銀行");

        // ★ 1. リストの定義
        List<String> accountTypeOptions = List.of(
                "住宅ローン",
                "マイカーローン",
                "教育ローン",
                "フリーローン",
                "カードローン"
        );

        // ★ 2. Modelへ登録
        model.addAttribute("accountTypeOptions", accountTypeOptions);

        // ★ 3. return は最後に書く
        return "bankLoanMain";
    }

    // 2. 確認画面の表示
    @PostMapping("/bankLoanConfirmation")
    public String confirmation(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        // 債務者名（旧：口座名義）が未入力の場合のデフォルト値設定
        if (bankLoanForm.getDebtorName() == null || bankLoanForm.getDebtorName().isEmpty()) {
            bankLoanForm.setDebtorName("ながれぼし銀行");
        }

        model.addAttribute("bankLoanApplication", bankLoanForm);
        return "bankLoanConfirmation";
    }

    // 3. 申込完了処理
    @PostMapping("/bankLoanCompletion")
    public String completion(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        applyBankLoanService.applyBankLoan(bankLoanForm);
        return "bankLoanCompletion";
    }
}