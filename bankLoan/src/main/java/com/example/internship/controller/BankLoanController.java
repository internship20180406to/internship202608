package com.example.internship.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.internship.entity.BankLoanForm;
import com.example.internship.service.ApplyBankLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

        // ★ 変更：架空の銀行名リストに変更
        model.addAttribute("nameOptions", List.of("テスト銀行", "サンプル中央銀行", "デモ信用金庫"));

        // リストの定義
        List<String> accountTypeOptions = List.of(
                "住宅ローン",
                "マイカーローン",
                "教育ローン",
                "フリーローン",
                "カードローン"
        );
        model.addAttribute("accountTypeOptions", accountTypeOptions);

        // 預金種別の選択肢を設定
        List<String> depositTypeOptions = List.of(
                "普通預金",
                "当座預金",
                "貯蓄預金"
        );
        model.addAttribute("depositTypeOptions", depositTypeOptions);

        return "bankLoanMain";
    }

    // 2. 確認画面の表示
    @PostMapping("/bankLoanConfirmation")
    public String confirmation(@ModelAttribute("bankLoanApplication") BankLoanForm bankLoanForm,
                               BindingResult bindingResult,
                               Model model) {

        // ★ 1. サーバー側：生年月日チェック（満20歳未満かどうかの判定）
        boolean hasAgeError = false;
        if (bankLoanForm.getBirthDate() != null && !bankLoanForm.getBirthDate().isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(bankLoanForm.getBirthDate());
                LocalDate today = LocalDate.now();
                int age = Period.between(birthDate, today).getYears();

                if (age < 20) {
                    bindingResult.rejectValue("birthDate", "error.birthDate",
                            "満20歳未満の方はローンを申し込むことができません。");
                    hasAgeError = true;
                }
            } catch (Exception e) {
                bindingResult.rejectValue("birthDate", "error.birthDate",
                        "生年月日の形式が正しくありません。");
                hasAgeError = true;
            }
        }

        // ★ 2. サーバー側：借入金額が年収の3分の1を超えていないかチェック
        boolean hasIncomeError = false;
        if (bankLoanForm.getAnnualIncome() != null && bankLoanForm.getLoanAmount() != null) {
            long incomeYen = bankLoanForm.getAnnualIncome() * 10000L; // 万円 → 円に換算
            long maxLimit = incomeYen / 3;                            // 年収の3分の1

            if (bankLoanForm.getLoanAmount() > maxLimit) {
                bindingResult.rejectValue("loanAmount", "error.loanAmount",
                        "借入金額は年収の3分の1（" + String.format("%,d", maxLimit) + "円）以下で入力してください。");
                hasIncomeError = true;
            }
        }

        // バリデーションエラー（未入力や各独自チェックのエラー）がある場合は、選択肢を再設定して入力画面に戻す
        if (bindingResult.hasErrors() || hasAgeError || hasIncomeError) {
            // ★ 変更：エラー時も架空の銀行名リストを再設定
            model.addAttribute("nameOptions", List.of("テスト銀行", "サンプル中央銀行", "デモ信用金庫"));

            model.addAttribute("accountTypeOptions", List.of("住宅ローン", "マイカーローン", "教育ローン", "フリーローン", "カードローン"));
            model.addAttribute("depositTypeOptions", List.of("普通預金", "当座預金", "貯蓄預金"));

            return "bankLoanMain";
        }

        if (bankLoanForm.getName() == null || bankLoanForm.getName().isEmpty()) {
            bankLoanForm.setName("ながれぼし銀行");
        }

        // 確認画面表示時の日時を自動生成してModelに登録
        String applicationDate = getNowDateTime();
        model.addAttribute("applicationDate", applicationDate);

        model.addAttribute("bankLoanApplication", bankLoanForm);
        return "bankLoanConfirmation";
    }

    // 3. 申込完了処理
    @PostMapping("/bankLoanCompletion")
    public String completion(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        // 申込確定時の日時を自動生成してModelに登録
        String applicationDate = getNowDateTime();
        model.addAttribute("applicationDate", applicationDate);

        applyBankLoanService.applyBankLoan(bankLoanForm);
        return "bankLoanCompletion";
    }

    // 現在日時を「yyyy/MM/dd HH:mm:ss」形式で取得する共通メソッド
    private String getNowDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return now.format(formatter);
    }
}