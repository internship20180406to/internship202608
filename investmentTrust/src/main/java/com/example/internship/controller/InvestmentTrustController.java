package com.example.internship.controller;

import com.example.internship.constant.InvestmentTrustOptions;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.OrderInvestmentTrustService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


@Controller
public class InvestmentTrustController {

    /** 入力画面のビュー名。入力エラー時の差し戻し先としても使う */
    private static final String MAIN_VIEW = "investmentTrustMain";

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    /**
     * 送信された文字列の前後の空白を取り除き、空文字はnullに変換する。
     * 「空白だけの入力」をフロント側と同じ扱いにするための設定。
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // プルダウンの選択肢は入力画面を表示するたびに必要になる。
    // @ModelAttribute を付けたメソッドはこのコントローラの全ハンドラの実行前に呼ばれるので、
    // 入力エラーで画面を戻すときも選択肢が消えない。
    @ModelAttribute("nameOptions")
    public List<String> nameOptions() {
        return InvestmentTrustOptions.BANK_NAMES;
    }

    @ModelAttribute("branchOptions")
    public List<String> branchOptions() {
        return InvestmentTrustOptions.BRANCH_NAMES;
    }

    @ModelAttribute("typeOptions")
    public List<String> typeOptions() {
        return InvestmentTrustOptions.ACCOUNT_TYPES;
    }

    @ModelAttribute("fundOptions")
    public List<String> fundOptions() {
        return InvestmentTrustOptions.FUND_NAMES;
    }

    @GetMapping("/investmentTrust")
    public String bankTransfer(Model model) {
        model.addAttribute("investmentTrustApplication", new InvestmentTrustForm());
        return MAIN_VIEW;
    }

    @PostMapping("/investmentTrustConfirmation")
    public String confirmation(
            @Valid @ModelAttribute("investmentTrustApplication") InvestmentTrustForm investmentTrustForm,
            BindingResult bindingResult) {
        validateSelectedOptions(investmentTrustForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return MAIN_VIEW;   // エラーがあれば確認画面に進ませず、入力画面に戻して理由を表示する
        }
        return "investmentTrustConfirmation";
    }

    @PostMapping("/investmentTrustCompletion")
    public String completion(
            @Valid @ModelAttribute("investmentTrustApplication") InvestmentTrustForm investmentTrustForm,
            BindingResult bindingResult) {
        // 確認画面はhidden項目で値を持ち回っているだけなので、登録直前にもう一度検証する
        validateSelectedOptions(investmentTrustForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return MAIN_VIEW;
        }
        orderInvestmentTrustService.orderInvestmentTrust(investmentTrustForm);
        return "investmentTrustCompletion";
    }

    /** プルダウンに存在しない値が送られてきていないかを確認する */
    private void validateSelectedOptions(InvestmentTrustForm form, BindingResult bindingResult) {
        rejectIfNotAllowed(bindingResult, "bankName", form.getBankName(),
                InvestmentTrustOptions.BANK_NAMES, "金融機関名を選択してください。");
        rejectIfNotAllowed(bindingResult, "branchName", form.getBranchName(),
                InvestmentTrustOptions.BRANCH_NAMES, "支店名を選択してください。");
        rejectIfNotAllowed(bindingResult, "bankAccountType", form.getBankAccountType(),
                InvestmentTrustOptions.ACCOUNT_TYPES, "科目名を選択してください。");
        rejectIfNotAllowed(bindingResult, "fundName", form.getFundName(),
                InvestmentTrustOptions.FUND_NAMES, "銘柄を選択してください。");
    }

    private void rejectIfNotAllowed(BindingResult bindingResult, String field, String value,
                                    List<String> allowedValues, String message) {
        if (bindingResult.hasFieldErrors(field)) {
            return;     // 未入力エラーなどが既に付いている項目に、メッセージを重ねて出さない
        }
        // List.of で作った不変リストは contains(null) でNPEになるため、nullを先に判定する
        if (value == null || !allowedValues.contains(value)) {
            bindingResult.rejectValue(field, "invalidOption", message);
        }
    }

}
