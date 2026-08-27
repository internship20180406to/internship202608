package com.example.internship.controller;

import com.example.internship.constant.InvestmentTrustOptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * チャット形式の申込画面。
 *
 * 従来のフォーム版（/investmentTrust）はそのまま残してある。
 * 送信先も判定も共通なので、両方を並べて比べられるし、いつでも片方を捨てられる。
 *
 * この画面はGETで枠を返すだけで、値のやり取りはすべてJSからAPIで行う。
 *   GET  /api/banks/...                 金融機関・支店の候補と照会
 *   GET  /api/accounts                  口座の名義と残高
 *   POST /api/investmentTrust/order     申込の確定
 */
@Controller
public class InvestmentTrustChatController {

    /** 科目・銘柄の選択肢は、会話の中で選択ボタンとして出すために画面へ渡す */
    @ModelAttribute("typeOptions")
    public List<String> typeOptions() {
        return InvestmentTrustOptions.ACCOUNT_TYPES;
    }

    @ModelAttribute("fundOptions")
    public List<String> fundOptions() {
        return InvestmentTrustOptions.FUND_NAMES;
    }

    @GetMapping("/investmentTrustChat")
    public String chat() {
        return "investmentTrustChat";
    }
}
