package com.example.internship.validation;

import com.example.internship.constant.InvestmentTrustOptions;
import com.example.internship.entity.AccountBalance;
import com.example.internship.entity.Bank;
import com.example.internship.entity.Branch;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.AccountBalanceService;
import com.example.internship.service.BankMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

/**
 * 投資信託の申込内容のうち、アノテーションでは判定できない部分を確認する。
 *
 * 判定するのは次の3つ。
 *   ・金融機関コード・支店コードがマスタに実在するか（あわせて名称をフォームに詰める）
 *   ・科目・銘柄が画面の選択肢に含まれる値か
 *   ・口座が実在し、残高が購入金額に足りているか
 *
 * ★このクラスを独立させている理由:
 *   画面（InvestmentTrustController）とチャットUIのAPI（InvestmentTrustApiController）の
 *   両方から同じ判定を使うため。
 *   入口ごとに判定を書くと、片方だけ直して抜け道ができる。判定は必ずここ1か所に置く。
 */
@Component
public class InvestmentTrustValidator {

    /**
     * 口座を特定する4項目。
     * この4つは「組み合わせ」で1つの口座を指すので、口座が見つからないときは
     * まとめてエラーにする（rejectAccountCombination を参照）。
     */
    private static final List<String> ACCOUNT_KEY_FIELDS =
            List.of("bankCode", "branchCode", "bankAccountType", "bankAccountNum");

    @Autowired
    private BankMasterService bankMasterService;

    @Autowired
    private AccountBalanceService accountBalanceService;

    /**
     * すべての判定を行い、対象の口座を返す。
     * エラーがあって口座を特定できない場合はnullを返す。
     */
    public AccountBalance validate(InvestmentTrustForm form, BindingResult bindingResult) {
        validateAndResolveMaster(form, bindingResult);
        rejectIfNotAllowed(bindingResult, "bankAccountType", form.getBankAccountType(),
                InvestmentTrustOptions.ACCOUNT_TYPES, "科目名を選択してください。");
        rejectIfNotAllowed(bindingResult, "fundName", form.getFundName(),
                InvestmentTrustOptions.FUND_NAMES, "銘柄を選択してください。");
        return validateAccountAndBalance(form, bindingResult);
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
        boolean keyBroken = ACCOUNT_KEY_FIELDS.stream().anyMatch(bindingResult::hasFieldErrors);
        if (keyBroken) {
            return null;
        }

        Optional<AccountBalance> account = accountBalanceService.findByForm(form);
        if (account.isEmpty()) {
            rejectAccountCombination(bindingResult,
                    "入力された口座は登録されていません。金融機関・支店・科目・口座番号の組み合わせをご確認ください。");
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
     * 口座を特定する4項目をまとめてエラーにする。
     *
     * 「口座が見つからない」のは4つの値の“組み合わせ”に対するエラーで、
     * どれか1つが間違っていると分かっているわけではない。
     * 口座番号だけを赤くすると「口座番号が間違っている」と読み取られてしまうため、
     *   ・理由はフォーム全体のエラーとして1回だけ表示する
     *   ・4項目は色（赤枠）だけで示し、同じ文言を4回並べない
     * という形にしている。
     */
    private void rejectAccountCombination(BindingResult bindingResult, String message) {
        bindingResult.reject("accountNotFound", message);
        // メッセージを空文字にすると、項目の下には何も出ず赤枠だけが付く
        ACCOUNT_KEY_FIELDS.forEach(field -> bindingResult.rejectValue(field, "accountNotFound", ""));
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
