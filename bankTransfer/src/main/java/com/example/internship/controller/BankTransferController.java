package com.example.internship.controller;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.service.ApplyBankTransferService;
import com.example.internship.entity.BankTransferFavoriteForm;
import com.example.internship.service.ApplyBankTransferFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Arrays;


@Controller
public class BankTransferController {

    @Autowired
    private ApplyBankTransferService applyBankTransferService;
    @Autowired
    private ApplyBankTransferFavoriteService applyBankTransferFavoriteService;

    @GetMapping("/bankTransfer")
    public String bankTransfer(Model model) {
        model.addAttribute("bankTransferApplication", new BankTransferForm());
        model.addAttribute("nameOptions", Arrays.asList("山陰共同銀行", "さくら未来銀行", "ながれぼし銀行", "ひかり中央", "ほしぞら銀行"));
        model.addAttribute("branchNameOptions", Arrays.asList("博多支店", "天神支店", "小倉支店", "久留米支店", "八女支店"));
        model.addAttribute("accountTypeOptions", Arrays.asList("普通預金", "当座預金", "貯蓄預金", "定期預金"));
        return "bankTransferMain";
    }

    @GetMapping("/bankTransferFavorite")
    public String bankTransferFavorite(Model model) {

        model.addAttribute(
                "favoriteList",
                applyBankTransferFavoriteService.getFavorites()
        );

        return "bankTransferFavorite";
    }

    @GetMapping("/bankTransfer/useFavorite")
    public String useFavorite(
            @RequestParam String bankName,
            @RequestParam String branchName,
            @RequestParam String bankAccountType,
            @RequestParam String bankAccountNum,
            @RequestParam String name,
            Model model) {

        BankTransferForm form = new BankTransferForm();

        form.setBankName(bankName);
        form.setBranchName(branchName);
        form.setBankAccountType(bankAccountType);
        form.setBankAccountNum(Integer.valueOf(bankAccountNum));
        form.setName(name);

        model.addAttribute("bankTransferApplication", form);
        model.addAttribute("nameOptions", Arrays.asList("山陰共同銀行", "さくら未来銀行", "ながれぼし銀行", "ひかり中央", "ほしぞら銀行"));
        model.addAttribute("branchNameOptions", Arrays.asList("博多支店", "天神支店", "小倉支店", "久留米支店", "八女支店"));
        model.addAttribute("accountTypeOptions", Arrays.asList("普通預金", "当座預金", "貯蓄預金", "定期預金"));
        return "bankTransferMain";
    }

    @PostMapping("/bankTransferFavorite")
    public String registerFavorite() {
        return "bankTransferFavorite";
    }


    @PostMapping("/bankTransferConfirmation")
    public String confirmation(@ModelAttribute BankTransferForm bankTransferForm, Model model) {

        Integer money = bankTransferForm.getMoney();

        if (money == null || money < 1 || money > 1000000) {
            model.addAttribute("errorMessage", "振込金額は1円以上100万円以下で入力してください。");
            model.addAttribute("bankTransferApplication", bankTransferForm);
            return "bankTransferMain";
        }

        model.addAttribute("bankName", bankTransferForm.getBankName());
        model.addAttribute("branchName", bankTransferForm.getBranchName());
        model.addAttribute("bankAccountType", bankTransferForm.getBankAccountType());
        model.addAttribute("bankAccountNum", bankTransferForm.getBankAccountNum());
        model.addAttribute("namer", bankTransferForm.getName());
        model.addAttribute("money", bankTransferForm.getMoney());
        model.addAttribute("transferDateTime", bankTransferForm.getTransferDateTime());
        model.addAttribute("bankTransferApplication", bankTransferForm);
        return "bankTransferConfirmation";
    }

    @PostMapping("/bankTransferCompletion")
    public String completion(@ModelAttribute BankTransferForm bankTransferForm, Model model) {
        applyBankTransferService.applyBankTransfer(bankTransferForm);
        model.addAttribute("bankTransferApplication", bankTransferForm);
        return "bankTransferCompletion";
    }

    @PostMapping("/bankTransfer/favorite")
    public String registerFavorite(@ModelAttribute BankTransferFavoriteForm form) {
        applyBankTransferFavoriteService.registerFavorite(form);
        return "redirect:/bankTransfer";
    }

}
