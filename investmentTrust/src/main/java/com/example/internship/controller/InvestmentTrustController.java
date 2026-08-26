package com.example.internship.controller;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.OrderInvestmentTrustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class InvestmentTrustController {

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;


    /*
     * =========================
     * 投資信託注文入力画面
     * =========================
     */
    @GetMapping("/investmentTrust")
    public String bankTransfer(Model model) {

        model.addAttribute(
                "investmentTrustApplication",
                new InvestmentTrustForm()
        );

        setOptions(model);

        /*
         * 最初に表示するSTEP
         */
        model.addAttribute("targetStep", 1);

        /*
         * 通常の新規入力
         */
        model.addAttribute("editMode", false);

        return "investmentTrustMain";
    }


    /*
     * =========================
     * 投資信託履歴画面
     * =========================
     */
    @GetMapping("/investmentTrustHistory")
    public String history() {

        return "rireki";
    }


    /*
     * =========================
     * 確認画面
     * =========================
     */
    @PostMapping("/investmentTrustConfirmation")
    public String confirmation(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            Model model) {

        model.addAttribute(
                "investmentTrustApplication",
                investmentTrustForm
        );

        return "investmentTrustConfirmation";
    }


    /*
     * =========================
     * 確認画面から修正
     * =========================
     */
    @PostMapping("/investmentTrustEdit")
    public String edit(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            @RequestParam("targetStep") int targetStep,
            Model model) {

        /*
         * 現在の入力内容を保持したまま
         * 修正したいSTEPを表示する
         */
        model.addAttribute(
                "investmentTrustApplication",
                investmentTrustForm
        );

        /*
         * 修正対象のSTEP
         */
        model.addAttribute(
                "targetStep",
                targetStep
        );

        /*
         * 選択肢を再設定
         */
        setOptions(model);

        /*
         * 修正モード
         */
        model.addAttribute(
                "editMode",
                true
        );

        return "investmentTrustMain";
    }


    /*
     * =========================
     * 注文完了
     * =========================
     */
    @PostMapping("/investmentTrustCompletion")
    public String completion(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            Model model) {

        orderInvestmentTrustService
                .orderInvestmentTrust(investmentTrustForm);

        return "investmentTrustCompletion";
    }


    /*
     * =========================
     * 選択肢設定
     * =========================
     */
    private void setOptions(Model model) {

        List<String> bankNameOptions = List.of(
                "山陰共同銀行",
                "ながれぼし銀行",
                "その他"
        );

        List<String> branchNameOptions = List.of(
                "本店",
                "福岡支店",
                "その他"
        );

        List<String> bankAccountTypeOptions = List.of(
                "普通",
                "当座",
                "貯蓄"
        );

        List<String> fundNameOptions = List.of(
                "A株式会社",
                "B株式会社",
                "C株式会社",
                "D株式会社"
        );

        model.addAttribute(
                "bankNameOptions",
                bankNameOptions
        );

        model.addAttribute(
                "branchNameOptions",
                branchNameOptions
        );

        model.addAttribute(
                "bankAccountTypeOptions",
                bankAccountTypeOptions
        );

        model.addAttribute(
                "fundNameOptions",
                fundNameOptions
        );
    }
}