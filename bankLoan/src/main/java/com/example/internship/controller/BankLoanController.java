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

    // 0. 【ホームページ】 http://localhost:8081/bankLoan にアクセスしたときに最初に表示
    @GetMapping("/bankLoan")
    public String index() {
        return "index"; // src/main/resources/templates/index.html を表示
    }

    // 1. 【入力画面】 「お申込みへ進む」ボタンを押したときに表示するURL
    @GetMapping("/bankLoan/form")
    public String bankTransfer(Model model) {
        model.addAttribute("bankLoanApplication", new BankLoanForm());

        // 銀行名の選択肢を設定
        model.addAttribute("nameOptions", List.of("テスト銀行", "サンプル中央銀行", "デモ信用金庫"));

        // ローン種類の選択肢を設定
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

        // ローン年数の選択肢を設定
        List<String> loanYearsOptions = List.of(
                "1年", "3年", "5年", "10年", "15年", "20年", "25年", "30年", "35年"
        );
        model.addAttribute("loanYearsOptions", loanYearsOptions);

        return "bankLoanMain";
    }

    // ★ 確認画面から「修正」ボタンで戻ってきたとき（POST）の処理
    @PostMapping("/bankLoan/edit")
    public String returnToInput(@ModelAttribute("bankLoanApplication") BankLoanForm bankLoanForm, Model model) {
        model.addAttribute("nameOptions", List.of("テスト銀行", "サンプル中央銀行", "デモ信用金庫"));
        model.addAttribute("accountTypeOptions", List.of("住宅ローン", "マイカーローン", "教育ローン", "フリーローン", "カードローン"));
        model.addAttribute("depositTypeOptions", List.of("普通預金", "当座預金", "貯蓄預金"));
        model.addAttribute("loanYearsOptions", List.of("1年", "3年", "5年", "10年", "15年", "20年", "25年", "30年", "35年"));

        return "bankLoanMain";
    }

    // 2. 確認画面の表示
    @PostMapping("/bankLoanConfirmation")
    public String confirmation(@ModelAttribute("bankLoanApplication") BankLoanForm bankLoanForm,
                               BindingResult bindingResult,
                               Model model) {

        // 生年月日チェック（満20歳未満かどうかの判定）
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

        // ローン種類ごとの借入金額・上限下限チェック
        boolean hasIncomeError = false;
        if (bankLoanForm.getLoanAmount() != null) {
            long loanAmountYen = bankLoanForm.getLoanAmount();
            String accountType = bankLoanForm.getAccountType();

            long minLimit = 10_000L;
            long maxLimit = 100_000_000L;
            String errorMessage = "借入金額の範囲が正しくありません。";

            if ("住宅ローン".equals(accountType)) {
                minLimit = 1_000_000L;
                maxLimit = 100_000_000L;
                errorMessage = "住宅ローンの借入金額は100万円以上、1億円以下で入力してください。";
            } else if ("マイカーローン".equals(accountType)) {
                minLimit = 100_000L;
                maxLimit = 5_000_000L;
                errorMessage = "マイカーローンの借入金額は10万円以上、500万円以下で入力してください。";
            } else if ("教育ローン".equals(accountType)) {
                minLimit = 100_000L;
                maxLimit = 3_000_000L;
                errorMessage = "教育ローンの借入金額は10万円以上、300万円以下で入力してください。";
            } else if ("フリーローン".equals(accountType)) {
                minLimit = 50_000L;
                maxLimit = 2_000_000L;
                errorMessage = "フリーローンの借入金額は5万円以上、200万円以下で入力してください。";
            } else if ("カードローン".equals(accountType)) {
                minLimit = 10_000L;
                maxLimit = 1_000_000L;
                errorMessage = "カードローンの借入金額は1万円以上、100万円以下で入力してください。";
            }

            if (loanAmountYen < minLimit || loanAmountYen > maxLimit) {
                bindingResult.rejectValue("loanAmount", "error.loanAmount", errorMessage);
                hasIncomeError = true;
            }
        }

        if (bindingResult.hasErrors() || hasAgeError || hasIncomeError) {
            model.addAttribute("nameOptions", List.of("テスト銀行", "サンプル中央銀行", "デモ信用金庫"));
            model.addAttribute("accountTypeOptions", List.of("住宅ローン", "マイカーローン", "教育ローン", "フリーローン", "カードローン"));
            model.addAttribute("depositTypeOptions", List.of("普通預金", "当座預金", "貯蓄預金"));
            model.addAttribute("loanYearsOptions", List.of("1年", "3年", "5年", "10年", "15年", "20年", "25年", "30年", "35年"));

            return "bankLoanMain";
        }

        if (bankLoanForm.getName() == null || bankLoanForm.getName().isEmpty()) {
            bankLoanForm.setName("ながれぼし銀行");
        }

        String applicationDate = getNowDateTime();
        model.addAttribute("applicationDate", applicationDate);

        model.addAttribute("bankLoanApplication", bankLoanForm);
        return "bankLoanConfirmation";
    }

    // 3. 申込完了処理
    @PostMapping("/bankLoanCompletion")
    public String completion(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        String applicationDate = getNowDateTime();
        model.addAttribute("applicationDate", applicationDate);
        model.addAttribute("bankLoanApplication", bankLoanForm);

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