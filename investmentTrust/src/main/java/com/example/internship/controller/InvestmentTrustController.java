package com.example.internship.controller;

import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.OrderInvestmentTrustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


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

    @PostMapping("/investmentTrustCompletion")
    public String completion(@ModelAttribute InvestmentTrustForm investmentTrustForm, Model model) {

//        System.out.println("===== 申込データ =====");
//       System.out.println("銀行名：" + investmentTrustForm.getBankName());
//        System.out.println("口座番号：" + investmentTrustForm.getBankAccountNum());
//        System.out.println("購入者名：" + investmentTrustForm.getPurchaserName());
//        System.out.println("銘柄：" + investmentTrustForm.getInvestmentTrustName());
//        System.out.println("科目名：" + investmentTrustForm.getBankSubject());
//        System.out.println("支店名：" + investmentTrustForm.getBranch());
//       System.out.println("購入金額：" + investmentTrustForm.getPurchaseAmount());
//
//       System.out.println("===== Service呼び出し完了 =====");


        orderInvestmentTrustService.orderInvestmentTrust(investmentTrustForm);
        return "investmentTrustCompletion";
    }

}
