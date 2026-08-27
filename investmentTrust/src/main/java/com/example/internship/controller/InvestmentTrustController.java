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

import java.util.List;

@Controller
public class InvestmentTrustController {

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    @Autowired
    private InvestmentTrustRepository investmentTrustRepository;


    /**
     * 投資信託注文入力画面
     */
    @GetMapping("/investmentTrust")
    public String investmentTrust(Model model) {

        model.addAttribute(
                "investmentTrustApplication",
                new InvestmentTrustForm()
        );

        setOptions(model);

        model.addAttribute(
                "targetStep",
                1
        );

        model.addAttribute(
                "editMode",
                false
        );

        return "investmentTrustMain";
    }


    /**
     * 注文履歴画面
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
     * 入力画面へ戻る
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
     * プルダウンの選択肢をDBから取得
     */
    private void setOptions(Model model) {

        /*
         * 金融機関名
         */
        List<String> bankNames =
                investmentTrustRepository.findBankNames();

        model.addAttribute(
                "bankNameOptions",
                bankNames
        );


        /*
         * 支店名
         */
        List<String> branchNames =
                investmentTrustRepository.findBranchNames();

        model.addAttribute(
                "branchNameOptions",
                branchNames
        );


        /*
         * 科目
         */
        List<String> bankAccountTypes =
                investmentTrustRepository.findBankAccountTypes();

        model.addAttribute(
                "bankAccountTypeOptions",
                bankAccountTypes
        );


        /*
         * 銘柄
         */
        List<Fund> funds =
                investmentTrustRepository.findFunds();

        model.addAttribute(
                "fundOptions",
                funds
        );
    }
}