package com.example.internship.controller;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.OrderInvestmentTrustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
//時間取得のためインポート
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class InvestmentTrustController {

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    @GetMapping("/investmentTrust")
    public String bankTransfer(Model model) {
        model.addAttribute("investmentTrustApplication", new InvestmentTrustForm());
//        model.addAttribute("nameOptions", "山陰共同銀行");
        return "investmentTrustMain";
    }

    @PostMapping("/investmentTrustConfirmation")
    public String confirmation(@ModelAttribute InvestmentTrustForm investmentTrustForm, Model model) {
//        investmentTrustForm.setBankName("ながれぼし銀行");
        model.addAttribute("bankName", investmentTrustForm.getBankName());
        model.addAttribute("bankAccountNum", investmentTrustForm.getBankAccountNum());
        model.addAttribute("investmentTrustApplication", investmentTrustForm);
        return "investmentTrustConfirmation";
    }

    // 入力内容を修正するときの処理
    @PostMapping("/investmentTrust")
    public String edit(@ModelAttribute InvestmentTrustForm investmentTrustForm,
                       Model model) {

        // 入力されていた内容を入力画面に渡す
        model.addAttribute(
                "investmentTrustApplication",
                investmentTrustForm
        );

        // 入力画面に戻る
        return "investmentTrustMain";
    }

    @PostMapping("/investmentTrustCompletion")
    public String completion(@ModelAttribute InvestmentTrustForm investmentTrustForm, Model model) {

        orderInvestmentTrustService.orderInvestmentTrust(investmentTrustForm);

        // 注文日時を取得
        LocalDateTime now = LocalDateTime.now();

        // 注文日時の表示形式を設定
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm");

        // 注文日時を文字列に変換
        String orderDate = now.format(formatter);

        // 完了画面に注文日時を渡す
        model.addAttribute("orderDate", orderDate);

        return "investmentTrustCompletion";
    }

}