package com.example.internship.controller;

import com.example.internship.constant.InvestmentTrustOptions;
import com.example.internship.entity.AccountRegistrationForm;
import com.example.internship.entity.Bank;
import com.example.internship.entity.Branch;
import com.example.internship.service.BankMasterService;
import com.example.internship.service.RegisterAccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DuplicateKeyException;
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

/**
 * 口座登録画面。
 *
 * 申込は account_balance に登録されている口座でないと行えないため、
 * その口座を用意するための画面。
 *
 * 画面の作りは投資信託の申込画面と同じ方針で揃えている。
 *   ・金融機関と支店はコードで指定し、名称はサーバがマスタから引き直す
 *   ・登録後はリダイレクトしてから完了画面を表示する（PRGパターン）
 */
@Controller
public class AccountRegistrationController {

    /** 入力画面のビュー名。入力エラー時の差し戻し先としても使う */
    private static final String MAIN_VIEW = "accountRegistrationMain";

    /** 画面上のフォームの名前 */
    private static final String FORM_NAME = "accountRegistration";

    @Autowired
    private BankMasterService bankMasterService;

    @Autowired
    private RegisterAccountService registerAccountService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    /** 科目のラジオボタンの選択肢。申込画面と同じ定数を使う */
    @ModelAttribute("typeOptions")
    public List<String> typeOptions() {
        return InvestmentTrustOptions.ACCOUNT_TYPES;
    }

    @GetMapping("/accountRegistration")
    public String form(Model model) {
        model.addAttribute(FORM_NAME, new AccountRegistrationForm());
        return MAIN_VIEW;
    }

    @PostMapping("/accountRegistrationCompletion")
    public String register(
            @Valid @ModelAttribute(FORM_NAME) AccountRegistrationForm form,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        validateMaster(form, bindingResult, model);
        validateNotRegistered(form, bindingResult);

        if (bindingResult.hasErrors()) {
            return MAIN_VIEW;
        }
        try {
            registerAccountService.registerAccount(form);
        } catch (DuplicateKeyException e) {
            // 存在チェックから登録までの隙間に、同じ口座が登録された場合。
            // 最終的に重複を防いでいるのはDBの主キー。
            bindingResult.rejectValue("accountNum", "duplicated", "この口座は既に登録されています。");
            return MAIN_VIEW;
        }
        redirectAttributes.addFlashAttribute("registeredAccount", form);
        redirectAttributes.addFlashAttribute("registeredBankName", model.getAttribute("bankName"));
        redirectAttributes.addFlashAttribute("registeredBranchName", model.getAttribute("branchName"));
        return "redirect:/accountRegistrationCompletion";
    }

    /**
     * 完了画面。
     * 直前のPOSTからリダイレクトされてきたときだけ表示する。
     * 再読み込みや直接アクセスでは表示するものが無いので、入力画面に戻す。
     */
    @GetMapping("/accountRegistrationCompletion")
    public String completed(Model model) {
        if (!model.containsAttribute("registeredAccount")) {
            return "redirect:/accountRegistration";
        }
        return "accountRegistrationCompletion";
    }

    /**
     * 金融機関コード・支店コードが実在するかを確認し、画面表示用の名称をmodelに入れる。
     *
     * 申込画面の validateAndResolveMaster と同じ考え方。
     * 名称は画面から送られてきた値ではなく、必ずマスタから引き直す。
     */
    private void validateMaster(AccountRegistrationForm form, BindingResult bindingResult, Model model) {
        Optional<Bank> bank = Optional.empty();
        if (!bindingResult.hasFieldErrors("bankCode")) {
            bank = bankMasterService.findBank(form.getBankCode());
            if (bank.isEmpty()) {
                bindingResult.rejectValue("bankCode", "notFound", "該当する金融機関がありません。");
            }
        }
        model.addAttribute("bankName", bank.map(Bank::getBankName).orElse(null));

        // 金融機関が確定していないと「その銀行に実在する支店か」を判定できない
        Optional<Branch> branch = Optional.empty();
        if (bank.isPresent() && !bindingResult.hasFieldErrors("branchCode")) {
            branch = bankMasterService.findBranch(form.getBankCode(), form.getBranchCode());
            if (branch.isEmpty()) {
                bindingResult.rejectValue("branchCode", "notFound", "該当する支店がありません。");
            }
        }
        model.addAttribute("branchName", branch.map(Branch::getBranchName).orElse(null));
    }

    /** 同じ口座が既に登録されていないかを確認する */
    private void validateNotRegistered(AccountRegistrationForm form, BindingResult bindingResult) {
        // 口座は4点セットで決まるので、どれか1つでもエラーなら判定できない
        if (bindingResult.hasFieldErrors("bankCode") || bindingResult.hasFieldErrors("branchCode")
                || bindingResult.hasFieldErrors("accountType")
                || bindingResult.hasFieldErrors("accountNum")) {
            return;
        }
        if (registerAccountService.exists(form)) {
            bindingResult.rejectValue("accountNum", "duplicated", "この口座は既に登録されています。");
        }
    }
}
