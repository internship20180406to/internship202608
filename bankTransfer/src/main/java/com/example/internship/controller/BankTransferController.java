package com.example.internship.controller;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.service.ApplyBankTransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class BankTransferController {

    @Autowired
    private ApplyBankTransferService applyBankTransferService;

    // 金融機関名の選択肢（各メソッドの実行前に自動でModelへ格納される）
    @ModelAttribute("nameOptions")
    public List<String> nameOptions() {
        return List.of(
                "ながれぼし銀行",
                "そらいろ銀行",
                "つきのわ銀行",
                "こもれび銀行",
                "かぜまち銀行"
        );
    }

    // 申し込み入力画面の表示
    @GetMapping("/bankTransfer")
    public String bankTransfer(Model model) {
        model.addAttribute("bankTransferApplication", new BankTransferForm());
        return "bankTransferMain";
    }

    // 確認画面の表示（入力値の検証を行う）
    @PostMapping("/bankTransferConfirmation")
    public String confirmation(@Valid @ModelAttribute("bankTransferApplication") BankTransferForm bankTransferForm,
                               BindingResult bindingResult) {
        //項目に空白がなければ、次の画面に遷移、空白があればもとの画面に遷移
        if (bindingResult.hasErrors()) {
            return "bankTransferMain";
        }
        return "bankTransferConfirmation";
    }

    // 申し込みの確定（DBへ登録し完了画面へ）
    // リロードによる二重登録を防ぐため、登録後はリダイレクトする（PRGパターン）
    @PostMapping("/bankTransferCompletion")
    public String completion(@ModelAttribute BankTransferForm bankTransferForm) {
        applyBankTransferService.applyBankTransfer(bankTransferForm);
        return "redirect:/bankTransferCompletion";
    }

    // 完了画面の表示（リダイレクト先）
    @GetMapping("/bankTransferCompletion")
    public String completionView() {
        return "bankTransferCompletion";
    }
}