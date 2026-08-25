package com.example.internship.controller;

import com.example.internship.entity.BankLoanForm;
import com.example.internship.service.ApplyBankLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
public class BankLoanController {

    @Autowired
    private ApplyBankLoanService applyBankLoanService;

    @ModelAttribute("nameOptions")
    public List<String> nameOptions() {
        return List.of(
                "山陰共同銀行",
                "なないろ銀行",
                "桜中央銀行",
                "みなと未来信用銀行",
                "つばさ中央銀行"
        );
    }

    @GetMapping("/bankLoan")
    public String bankLoan(Model model) {
        model.addAttribute("bankLoanApplication", new BankLoanForm());

        return "bankLoanMain";
    }

    @PostMapping("/bankLoanConfirmation")
    public String confirmation(
            @Valid @ModelAttribute("bankLoanApplication") BankLoanForm bankLoanForm,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {

            return "bankLoanMain";

        }

        return "bankLoanConfirmation";
    }

    @PostMapping("/bankLoanCompletion")
    public String completion(
            @ModelAttribute("bankLoanApplication") BankLoanForm bankLoanForm,
            Model model) {
        applyBankLoanService.applyBankLoan(bankLoanForm);
        return "bankLoanCompletion";
    }

}
