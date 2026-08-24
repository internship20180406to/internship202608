package com.example.internship.controller;

import com.example.internship.entity.BankTransferForm;//振込に必要な情報を入れる箱
import com.example.internship.service.ApplyBankTransferService;//データベースの挿入を依頼する関数
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

//画面の遷移を支持する
@Controller
public class BankTransferController {

    @Autowired
    private ApplyBankTransferService applyBankTransferService;

    //申し込み画面を出力するよう指示
    @GetMapping("/bankTransfer")
    public String bankTransfer(Model model) {
        model.addAttribute("bankTransferApplication", new BankTransferForm());//入力欄の受け皿を用意する
        model.addAttribute("nameOptions", "山陰共同銀行");//プルダウンの中身を用意する（変更ポイント）
        return "bankTransferMain";
    }
//確認画面を表示するよう指示。入力値を受け取って，確認画面の材料として詰め直す
    @PostMapping("/bankTransferConfirmation")
    public String confirmation(@ModelAttribute BankTransferForm bankTransferForm, Model model) {
        model.addAttribute("bankName", bankTransferForm.getBankName());
        model.addAttribute("bankAccountNum", bankTransferForm.getBankAccountNum());
        model.addAttribute("bankTransferApplication", bankTransferForm);
        return "bankTransferConfirmation";
    }

    //入力内容をデータベースに保存し完了画面を表示するよう指示
    @PostMapping("/bankTransferCompletion")
    public String completion(@ModelAttribute BankTransferForm bankTransferForm, Model model) {
        applyBankTransferService.applyBankTransfer(bankTransferForm);//データの挿入を依頼
        return "bankTransferCompletion";
    }

}
