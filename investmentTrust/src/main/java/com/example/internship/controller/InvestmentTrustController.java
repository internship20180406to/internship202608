package com.example.internship.controller;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.OrderInvestmentTrustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class InvestmentTrustController {

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    @GetMapping("/investmentTrust")
    public String bankTransfer(Model model) {

        model.addAttribute("investmentTrustApplication", new InvestmentTrustForm());

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

        model.addAttribute("bankNameOptions", bankNameOptions);
        model.addAttribute("branchNameOptions", branchNameOptions);
        model.addAttribute("bankAccountTypeOptions", bankAccountTypeOptions);
        model.addAttribute("fundNameOptions", fundNameOptions);

        return "investmentTrustMain";
    }

    @PostMapping("/investmentTrustConfirmation")
    public String confirmation(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            Model model) {

        // investmentTrustForm.setBankName("ここをBankNameにしたい");

        model.addAttribute("bankName", investmentTrustForm.getBankName());
        model.addAttribute("bankAccountNum", investmentTrustForm.getBankAccountNum());
        model.addAttribute("investmentTrustApplication", investmentTrustForm);

        return "investmentTrustConfirmation";
    }

    @PostMapping("/investmentTrustCompletion")
    public String completion(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            Model model) {

        orderInvestmentTrustService.orderInvestmentTrust(investmentTrustForm);

        return "investmentTrustCompletion";
    }
}