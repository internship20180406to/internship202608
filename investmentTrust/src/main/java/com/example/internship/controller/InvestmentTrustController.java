package com.example.internship.controller;

import com.example.internship.entity.Fund;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.repository.InvestmentTrustRepository;
import com.example.internship.service.OrderInvestmentTrustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class InvestmentTrustController {

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    @Autowired
    private InvestmentTrustRepository investmentTrustRepository;


    /**
     * 投資信託注文画面
     */
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


    /**
     * 注文履歴
     */
    @GetMapping("/investmentTrustHistory")
    public String history(Model model) {

        model.addAttribute(
                "history",
                orderInvestmentTrustService.getHistory()
        );

        return "rireki";
    }


    /**
     * 確認画面
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


    /**
     * 編集
     */
    @PostMapping("/investmentTrustEdit")
    public String edit(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            @RequestParam("targetStep") int targetStep,
            Model model) {

        model.addAttribute(
                "investmentTrustApplication",
                investmentTrustForm
        );

        model.addAttribute(
                "targetStep",
                targetStep
        );

        setOptions(model);

        model.addAttribute(
                "editMode",
                true
        );

        return "investmentTrustMain";
    }


    /**
     * 注文完了
     */
    @PostMapping("/investmentTrustCompletion")
    public String completion(
            @ModelAttribute InvestmentTrustForm investmentTrustForm,
            Model model) {

        orderInvestmentTrustService.orderInvestmentTrust(
                investmentTrustForm
        );

        return "investmentTrustCompletion";
    }


    /**
     * 金融機関・支店・口座種別の選択肢を設定
     */
    private void setOptions(Model model) {

        model.addAttribute(
                "bankNameOptions",
                investmentTrustRepository.findBankNames()
        );

        model.addAttribute(
                "branchNameOptions",
                investmentTrustRepository.findBranchNames()
        );

        model.addAttribute(
                "bankAccountTypeOptions",
                investmentTrustRepository.findBankAccountTypes()
        );
    }


    /**
     * 銘柄検索API
     *
     * /api/funds?keyword=日本
     */
    @GetMapping("/api/funds")
    @ResponseBody
    public List<Fund> searchFunds(
            @RequestParam(
                    value = "keyword",
                    defaultValue = ""
            )
            String keyword) {

        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return List.of();
        }

        return investmentTrustRepository.searchFunds(
                keyword.trim()
        );
    }
}