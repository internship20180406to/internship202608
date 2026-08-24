// package このファイルはcom.example.internship.controllerの中に含まれる
package com.example.internship.controller;

// import 別のパッケージ（フォルダ）にある BankLoanForm クラスを使えるように読み込む
import com.example.internship.entity.BankLoanForm;
import com.example.internship.service.ApplyBankLoanService;

//Spring（フレームワーク）がサービスなどの部品（ApplyBankLoanService など）を自動で準備してセットする仕組み
import org.springframework.beans.factory.annotation.Autowired; //自動で部品を準備・割り当てる機能

import org.springframework.stereotype.Controller; //このクラスが画面遷移を制御する「窓口」だと宣言する機能

import org.springframework.ui.Model; //Javaのデータを画面（HTML）に受け渡す「バケツ」のような役割

import org.springframework.web.bind.annotation.GetMapping; //ページを開く（取得する）ときの処理を指定する機能

import org.springframework.web.bind.annotation.ModelAttribute; //画面から送られてきた入力データを受け取る機能

import org.springframework.web.bind.annotation.PostMapping; //フォーム等からデータを送る（保存・遷移する）ときの処理を指定する機能


//SpringがこのクラスをWeb画面の制御役として認識し、ユーザーからのアクセスを処理できるように準備する
@Controller
public class BankLoanController {

    @Autowired
    private ApplyBankLoanService applyBankLoanService;

//ブラウザで /bankLoan のページを開いたときに、入力フォームを準備して表示する
    @GetMapping("/bankLoan")
    public String bankTransfer(Model model) {
        model.addAttribute("bankLoanApplication", new BankLoanForm());
        model.addAttribute("nameOptions", "山陰共同銀行");//金融機関名
        return "bankLoanMain";
    }

//「入力画面で『確認』ボタンが押されたとき、送られたデータを受け取って確認画面を表示する準備をする
    @PostMapping("/bankLoanConfirmation")
    public String confirmation(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        if (bankLoanForm.getAccountHolder() == null || bankLoanForm.getAccountHolder().isEmpty()) {
            bankLoanForm.setAccountHolder("ながれぼし銀行"); //口座名義のデフォルト
        }

        // オブジェクトごと画面（Thymeleaf）へ渡します
        model.addAttribute("bankLoanApplication", bankLoanForm);

        return "bankLoanConfirmation";
    }

//確認画面で『申込』ボタンが押されたとき、ローンの申込処理を実行して完了画面を表示する
    @PostMapping("/bankLoanCompletion")
    public String completion(@ModelAttribute BankLoanForm bankLoanForm, Model model) {
        applyBankLoanService.applyBankLoan(bankLoanForm);
        return "bankLoanCompletion";
    }

}