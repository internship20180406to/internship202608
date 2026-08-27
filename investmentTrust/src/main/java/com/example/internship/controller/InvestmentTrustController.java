package com.example.internship.controller;

import com.example.internship.constant.InvestmentTrustOptions;
import com.example.internship.entity.AccountBalance;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.InsufficientBalanceException;
import com.example.internship.service.OrderInvestmentTrustService;
import com.example.internship.validation.InvestmentTrustValidator;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
public class InvestmentTrustController {

    /** 入力画面のビュー名。入力エラー時の差し戻し先としても使う */
    private static final String MAIN_VIEW = "investmentTrustMain";

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    /**
     * マスタ照会や残高確認といった、アノテーションでは書けない判定はここが持っている。
     * チャットUIのAPI（InvestmentTrustApiController）も同じものを使うので、
     * 判定を足すときは必ずこのクラス側に足すこと。
     */
    @Autowired
    private InvestmentTrustValidator investmentTrustValidator;

    /**
     * 送信された文字列の前後の空白を取り除き、空文字はnullに変換する。
     * 「空白だけの入力」をフロント側と同じ扱いにするための設定。
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // ラジオボタン・プルダウンの選択肢は入力画面を表示するたびに必要になる。
    // @ModelAttribute を付けたメソッドはこのコントローラの全ハンドラの実行前に呼ばれるので、
    // 入力エラーで画面を戻すときも選択肢が消えない。
    //
    // ※金融機関名・支店名の選択肢はここには無い。件数が増えても対応できるよう
    //   マスタテーブルに移し、画面ではコードを入力して名称をAjaxで引く方式にしたため。
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
            BindingResult bindingResult, Model model) {
        AccountBalance balance = investmentTrustValidator.validate(investmentTrustForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return MAIN_VIEW;   // エラーがあれば確認画面に進ませず、入力画面に戻して理由を表示する
        }
        // 申込前に、今の残高と引き落とし後の残高を見せる
        model.addAttribute("currentBalance", balance.getBalance());
        model.addAttribute("balanceAfter", balance.getBalance() - investmentTrustForm.getMoney());
        return "investmentTrustConfirmation";
    }

    /**
     * 申込を確定する。
     *
     * 処理後に画面をそのまま返さず、リダイレクトしてから完了画面を表示している（PRGパターン）。
     * POSTの結果をそのまま表示すると、ブラウザの再読み込みで同じPOSTがもう一度送られ、
     * 二重に申込・引き落としが行われてしまうため。
     * リダイレクト後はGETなので、何度再読み込みしても登録は起きない。
     *
     * 表示に必要な値は addFlashAttribute でリダイレクト先へ渡す。
     * flash attribute は1回だけ取り出せる一時的な入れ物で、URLに出ないので
     * 申込内容がアドレスバーに残らない。
     */
    @PostMapping("/investmentTrustCompletion")
    public String completion(
            @Valid @ModelAttribute("investmentTrustApplication") InvestmentTrustForm investmentTrustForm,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // 確認画面はhidden項目で値を持ち回っているだけなので、登録直前にもう一度検証する。
        // 金融機関名・支店名もここでマスタから引き直されるため、
        // hidden項目を書き換えて別の名称を登録させることはできない。
        investmentTrustValidator.validate(investmentTrustForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return MAIN_VIEW;
        }
        try {
            long balanceAfter = orderInvestmentTrustService.orderInvestmentTrust(investmentTrustForm);
            redirectAttributes.addFlashAttribute("completedOrder", investmentTrustForm);
            redirectAttributes.addFlashAttribute("balanceAfter", balanceAfter);
            return "redirect:/investmentTrustCompletion";
        } catch (InsufficientBalanceException e) {
            // 確認画面を見てから「申込」を押すまでの間に、別の申込で残高が減っていた場合など。
            // 上のチェックを通っていても、最終的な判定は引き落としのUPDATE文が行う。
            bindingResult.rejectValue("money", "insufficientBalance",
                    "残高が不足しています。もう一度ご確認ください。");
            return MAIN_VIEW;
        }
    }

    /**
     * 完了画面の表示。
     *
     * 直前のPOSTからリダイレクトされてきたときだけ表示するものがある。
     * 完了画面をブックマークして開いた場合や、リダイレクト後にもう一度再読み込みした場合は
     * flash attribute が空なので、入力画面に戻す。
     */
    @GetMapping("/investmentTrustCompletion")
    public String completed(Model model) {
        if (!model.containsAttribute("balanceAfter")) {
            return "redirect:/investmentTrust";
        }
        return "investmentTrustCompletion";
    }
}
