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

    @GetMapping("/investmentTrust")
    public String investmentTrust(Model model) {

        model.addAttribute(
                "investmentTrustApplication",
                new InvestmentTrustForm()
        );

        setOptions(model);

        model.addAttribute("targetStep", 1);
        model.addAttribute("editMode", false);

        return "investmentTrustMain";
    }

    @GetMapping("/investmentTrustHistory")
    public String history(Model model) {

        model.addAttribute(
                "history",
                orderInvestmentTrustService.getHistory()
        );

        return "rireki";
    }

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

    @PostMapping("/investmentTrustEdit")
    public String edit(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            @RequestParam("targetStep") int targetStep,
            Model model) {

        model.addAttribute(
                "investmentTrustApplication",
                investmentTrustForm
        );

        model.addAttribute("targetStep", targetStep);

        setOptions(model);

        model.addAttribute("editMode", true);

        return "investmentTrustMain";
    }

    @PostMapping("/investmentTrustCompletion")
    public String completion(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            Model model) {

        orderInvestmentTrustService.orderInvestmentTrust(
                investmentTrustForm
        );

        return "investmentTrustCompletion";
    }

    private void setOptions(Model model) {

        model.addAttribute(
                "bankNameOptions",
                List.of(
                        "山陰共同銀行",
                        "ながれぼし銀行",
                        "その他"
                )
        );

        model.addAttribute(
                "branchNameOptions",
                List.of(
                        "本店",
                        "福岡支店",
                        "その他"
                )
        );

        model.addAttribute(
                "bankAccountTypeOptions",
                List.of(
                        "普通",
                        "当座",
                        "貯蓄"
                )
        );

        model.addAttribute(
                "fundNameOptions",
                List.of(
                        "A株式会社",
                        "B株式会社",
                        "C株式会社",
                        "D株式会社"
                )
        );
    }
}