package com.example.internship.controller;

import com.example.internship.constant.InvestmentTrustOptions;
import com.example.internship.entity.AccountBalance;
import com.example.internship.entity.Bank;
import com.example.internship.entity.Branch;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.AccountBalanceService;
import com.example.internship.service.BankMasterService;
import com.example.internship.service.InsufficientBalanceException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;


@Controller
public class InvestmentTrustController {

    /** 入力画面のビュー名。入力エラー時の差し戻し先としても使う */
    private static final String MAIN_VIEW = "investmentTrustMain";

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    @Autowired
    private BankMasterService bankMasterService;

    @Autowired
    private AccountBalanceService accountBalanceService;

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
        AccountBalance balance = validateSelectedOptions(investmentTrustForm, bindingResult);
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
        validateSelectedOptions(investmentTrustForm, bindingResult);
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

    /**
     * 画面の選択肢・マスタに存在しない値が送られてきていないかを確認し、
     * 併せて対象の口座を返す。エラーがあって口座を特定できない場合はnullを返す。
     */
    private AccountBalance validateSelectedOptions(InvestmentTrustForm form, BindingResult bindingResult) {
        validateAndResolveMaster(form, bindingResult);
        rejectIfNotAllowed(bindingResult, "bankAccountType", form.getBankAccountType(),
                InvestmentTrustOptions.ACCOUNT_TYPES, "科目名を選択してください。");
        rejectIfNotAllowed(bindingResult, "fundName", form.getFundName(),
                InvestmentTrustOptions.FUND_NAMES, "銘柄を選択してください。");
        return validateAccountAndBalance(form, bindingResult);
    }

    /**
     * 口座が実在するかと、残高が購入金額に足りているかを確認する。
     *
     * ここでの残高チェックは「申込前に気づかせる」ためのもので、最終的な判定ではない。
     * この確認から実際の引き落としまでの間に別の申込で残高が減る可能性があるため、
     * 引き落とし自体もUPDATE文の中で残高を判定している
     * （AccountBalanceRepository#withdraw を参照）。
     */
    private AccountBalance validateAccountAndBalance(InvestmentTrustForm form, BindingResult bindingResult) {
        // 口座は「金融機関コード＋支店コード＋科目＋口座番号」の4点で決まるので、
        // どれか1つでもエラーになっていると口座を特定できない。
        if (bindingResult.hasFieldErrors("bankCode") || bindingResult.hasFieldErrors("branchCode")
                || bindingResult.hasFieldErrors("bankAccountType")
                || bindingResult.hasFieldErrors("bankAccountNum")) {
            return null;
        }

        Optional<AccountBalance> account = accountBalanceService.findByForm(form);
        if (account.isEmpty()) {
            bindingResult.rejectValue("bankAccountNum", "accountNotFound",
                    "該当する口座がありません。金融機関・支店・科目・口座番号をご確認ください。");
            return null;
        }

        // 金額そのものが不正（未入力・範囲外）なら、残高との比較はしない
        if (!bindingResult.hasFieldErrors("money") && account.get().getBalance() < form.getMoney()) {
            bindingResult.rejectValue("money", "insufficientBalance",
                    String.format("残高が不足しています。（残高: %,d円）", account.get().getBalance()));
        }
        return account.get();
    }

    /**
     * 金融機関コード・支店コードが実在するかをマスタに問い合わせ、
     * あわせて画面表示・登録に使う名称をフォームに詰める。
     *
     * 画面のJSもAjaxで同じことをしているが、JSは開発者ツールで無効化できるので、
     * ここでの確認が最終的な判定になる。
     *
     * 名称は「画面から送られてきた値」ではなく「今マスタに入っている値」を使う。
     * こうすることで、コードと名称が食い違った組み合わせを送り込まれても影響を受けない。
     */
    private void validateAndResolveMaster(InvestmentTrustForm form, BindingResult bindingResult) {
        // 金融機関:書式エラー（未入力・4桁でない）が既に付いているならマスタ照会はしない。
        // 1つの項目にメッセージを重ねて出さないため。
        Optional<Bank> bank = Optional.empty();
        if (!bindingResult.hasFieldErrors("bankCode")) {
            bank = bankMasterService.findBank(form.getBankCode());
            if (bank.isEmpty()) {
                bindingResult.rejectValue("bankCode", "notFound", "該当する金融機関がありません。");
            }
        }
        form.setBankName(bank.map(Bank::getBankName).orElse(null));

        // 支店:金融機関が確定していないと「その銀行に実在する支店か」を判定できないので、
        // 金融機関が引けなかった場合は支店の判定を行わない（先に金融機関を直してもらう）。
        Optional<Branch> branch = Optional.empty();
        if (bank.isPresent() && !bindingResult.hasFieldErrors("branchCode")) {
            branch = bankMasterService.findBranch(form.getBankCode(), form.getBranchCode());
            if (branch.isEmpty()) {
                bindingResult.rejectValue("branchCode", "notFound", "該当する支店がありません。");
            }
        }
        form.setBranchName(branch.map(Branch::getBranchName).orElse(null));
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
