package com.example.internship.controller;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
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

    private static final Map<String, BigDecimal> INTEREST_RATE_OPTIONS =
            new LinkedHashMap<>();

    static {
        INTEREST_RATE_OPTIONS.put(
                "変動金利",
                new BigDecimal("0.80")
        );

        INTEREST_RATE_OPTIONS.put(
                "固定金利10年",
                new BigDecimal("1.50")
        );

        INTEREST_RATE_OPTIONS.put(
                "全期間固定金利",
                new BigDecimal("2.00")
        );
    }

    @Autowired
    private ApplyBankLoanService applyBankLoanService;

    @GetMapping("/bankLoan")
    public String bankTransfer(Model model) {
        model.addAttribute("bankLoanApplication", new BankLoanForm());
        model.addAttribute("nameOptions", new String[]{"山陰共同銀行", "カウカウ銀行", "流れ星銀行"});
        model.addAttribute("branchOptions", new String[]{"本店営業部", "福岡支店", "博多支店"});
        model.addAttribute("subjectOptions", new String[]{"普通預金", "当座預金", "貯蓄預金"}
        );
        return "bankLoanMain";
    }

    @PostMapping("/bankLoanDetails")
    public String details(
            @ModelAttribute BankLoanForm bankLoanForm,
            Model model) {

        model.addAttribute("bankLoanApplication", bankLoanForm);
        model.addAttribute("rateOptions", INTEREST_RATE_OPTIONS);

        return "bankLoanDetails";
    }


    @PostMapping("/bankLoanConfirmation")
    public String confirmation(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        //金利タイプに対応する金利を設定
        BigDecimal interestRate =
                INTEREST_RATE_OPTIONS.get(
                        bankLoanForm.getInterestType()
                );
        bankLoanForm.setInterestRate(interestRate);
        //姓と名を結合
        bankLoanForm.setDebtorName(
                bankLoanForm.getDebtorLastName()
                        + " "
                        + bankLoanForm.getDebtorFirstName()
        );
        model.addAttribute("bankName", bankLoanForm.getBankName());
        model.addAttribute("bankAccountNum", bankLoanForm.getBankAccountNum());
        model.addAttribute("bankLoanApplication", bankLoanForm);
        return "bankLoanConfirmation";
    }

    @PostMapping("/bankLoanCompletion")
    public String completion(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        applyBankLoanService.applyBankLoan(bankLoanForm);
        return "bankLoanCompletion";
    }

}
