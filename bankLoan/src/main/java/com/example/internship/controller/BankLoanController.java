package com.example.internship.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        // ★ 預金種別（口座の科目）の選択肢を設定
        List<String> depositTypeOptions = List.of(
                "普通預金",
                "当座預金",
                "貯蓄預金"
        );
        model.addAttribute("depositTypeOptions", depositTypeOptions);

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

        // ★ 確認画面表示時の日時を自動生成してModelに登録
        String applicationDate = getNowDateTime();
        model.addAttribute("applicationDate", applicationDate);

        model.addAttribute("bankLoanApplication", bankLoanForm);
        return "bankLoanConfirmation";
    }

    // 3. 申込完了処理
    @PostMapping("/bankLoanCompletion")
    public String completion(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        // ★ 申込確定時の日時を自動生成してModelに登録
        String applicationDate = getNowDateTime();
        model.addAttribute("applicationDate", applicationDate);

        applyBankLoanService.applyBankLoan(bankLoanForm);
        return "bankLoanCompletion";
    }

    // ★ 現在日時を「yyyy/MM/dd HH:mm:ss」形式で取得する共通メソッド
    private String getNowDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return now.format(formatter);
    }
}