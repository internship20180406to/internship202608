package com.example.internship.controller;


import com.example.internship.entity.BankCustomerAccount;
import com.example.internship.service.BankAccountVerificationService;

import java.util.Collections;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import com.example.internship.entity.BankLoanForm;
import com.example.internship.service.ApplyBankLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class BankLoanController {

    private static final Logger log =
            LoggerFactory.getLogger(BankLoanController.class);

    private static final String SUBMISSION_TOKEN_KEY =
            "loanSubmissionToken";

    private static final String CONFIRMATION_FORM_KEY =
            "confirmationBankLoanApplication";

    private static final String COMPLETED_FORM_KEY =
            "completedBankLoanApplication";

    private static final String CONFIRMATION_ERROR_KEY =
            "loanConfirmationError";

    private static final String VERIFIED_ACCOUNT_KEY =
            "verifiedBankCustomerAccount";

    private static final Map<String, BigDecimal> INTEREST_RATE_OPTIONS =
            new LinkedHashMap<>();

    static {
        INTEREST_RATE_OPTIONS.put(
                "変動金利",
                new BigDecimal("0.80")
        );

        INTEREST_RATE_OPTIONS.put(
                "固定金利10年",
                new BigDecimal("1.50")
        );

        INTEREST_RATE_OPTIONS.put(
                "全期間固定金利",
                new BigDecimal("2.00")
        );
    }

    @Autowired
    private ApplyBankLoanService applyBankLoanService;

    @Autowired
    private BankAccountVerificationService
            bankAccountVerificationService;

    @GetMapping("/bankLoanSimulation")
    public String simulation(Model model) {
        model.addAttribute("rateOptions", INTEREST_RATE_OPTIONS);
        return "bankLoanSimulation";
    }

    @GetMapping("/bankLoan")
    public String bankTransfer(
            Model model,
            HttpSession session) {

        // 新しい申込なので以前の状態を削除
        session.removeAttribute(SUBMISSION_TOKEN_KEY);
        session.removeAttribute(CONFIRMATION_FORM_KEY);
        session.removeAttribute(COMPLETED_FORM_KEY);
        session.removeAttribute(CONFIRMATION_ERROR_KEY);
        session.removeAttribute(VERIFIED_ACCOUNT_KEY);

        BankLoanForm bankLoanForm =
                new BankLoanForm();

        // 今回はカウカウ銀行の口座保有者限定
        bankLoanForm.setBankName("カウカウ銀行");

        model.addAttribute(
                "bankLoanApplication",
                bankLoanForm
        );

        addAccountScreenOptions(model);

        log.info("event=loan_application_started");

        return "bankLoanMain";
    }

    // 確認画面から送られてきた入力内容をbankloanMainへ戻す
    @PostMapping("/bankLoanEdit")
    public String editBasicInformation(
            @ModelAttribute BankLoanForm bankLoanForm,
            Model model,
            HttpSession session) {

        // 口座を修正するため、以前の照合結果を無効化
        session.removeAttribute(VERIFIED_ACCOUNT_KEY);

        bankLoanForm.setBankName("カウカウ銀行");

        model.addAttribute(
                "bankLoanApplication",
                bankLoanForm
        );

        addAccountScreenOptions(model);

        return "bankLoanMain";
    }

    @PostMapping("/bankLoanDetails")
    public String details(
            @ModelAttribute BankLoanForm bankLoanForm,
            Model model,
            HttpSession session) {

        bankLoanForm.setBankName("カウカウ銀行");

        BankCustomerAccount verifiedAccount =
                bankAccountVerificationService
                        .verifyAccount(
                                bankLoanForm.getBranchName(),
                                bankLoanForm.getSubjectName(),
                                bankLoanForm.getBankAccountNum(),
                                bankLoanForm.getBirthDate()
                        )
                        .orElse(null);

        // 口座情報がマスタに存在しない場合
        if (verifiedAccount == null) {
            session.removeAttribute(
                    VERIFIED_ACCOUNT_KEY
            );

            log.warn(
                    "event=bank_account_verification_failed"
            );

            model.addAttribute(
                    "bankLoanApplication",
                    bankLoanForm
            );

            model.addAttribute(
                    "accountVerificationError",
                    "入力された口座情報を確認できませんでした。"
            );

            addAccountScreenOptions(model);

            return "bankLoanMain";
        }

        // DBから取得した情報をフォームへ設定
        bankLoanForm.setCustomerId(
                verifiedAccount.getCustomerId()
        );

        bankLoanForm.setAccountId(
                verifiedAccount.getAccountId()
        );

        bankLoanForm.setCustomerNumber(
                verifiedAccount.getCustomerNumber()
        );

        bankLoanForm.setDebtorLastName(
                verifiedAccount.getLastName()
        );

        bankLoanForm.setDebtorFirstName(
                verifiedAccount.getFirstName()
        );

        bankLoanForm.setDebtorLastNameKana(
                verifiedAccount.getLastNameKana()
        );

        bankLoanForm.setDebtorFirstNameKana(
                verifiedAccount.getFirstNameKana()
        );

        bankLoanForm.setBirthDate(
                verifiedAccount.getBirthDate()
        );

        bankLoanForm.setDebtorName(
                verifiedAccount.getLastName()
                        + " "
                        + verifiedAccount.getFirstName()
        );

        /*
         * 後続画面ではブラウザから送られた氏名ではなく、
         * この照合済み情報を使用する
         */
        session.setAttribute(
                VERIFIED_ACCOUNT_KEY,
                verifiedAccount
        );

        log.info(
                "event=bank_account_verified"
        );

        model.addAttribute(
                "bankLoanApplication",
                bankLoanForm
        );

        model.addAttribute(
                "rateOptions",
                INTEREST_RATE_OPTIONS
        );

        return "bankLoanDetails";
    }

    @PostMapping("/bankLoanConfirmation")
    public String confirmation(
            @ModelAttribute BankLoanForm bankLoanForm,
            HttpSession session) {

        // 口座確認時にサーバーへ保存した照合済み情報
        BankCustomerAccount verifiedAccount =
                (BankCustomerAccount) session.getAttribute(
                        VERIFIED_ACCOUNT_KEY
                );

        // 口座照合を通っていない場合は申込を続けさせない
        if (verifiedAccount == null) {
            log.warn(
                    "event=loan_application_unverified_account"
            );

            return "redirect:/bankLoan";
        }

        /*
         * ブラウザから送信された本人・口座情報を使用せず、
         * DBで照合済みの情報に戻す
         */
        bankLoanForm.setBankName(
                "カウカウ銀行"
        );

        bankLoanForm.setCustomerId(
                verifiedAccount.getCustomerId()
        );

        bankLoanForm.setAccountId(
                verifiedAccount.getAccountId()
        );

        bankLoanForm.setCustomerNumber(
                verifiedAccount.getCustomerNumber()
        );

        bankLoanForm.setBranchName(
                verifiedAccount.getBranchName()
        );

        bankLoanForm.setSubjectName(
                verifiedAccount.getAccountType()
        );

        bankLoanForm.setBankAccountNum(
                verifiedAccount.getAccountNumber()
        );

        bankLoanForm.setDebtorLastName(
                verifiedAccount.getLastName()
        );

        bankLoanForm.setDebtorFirstName(
                verifiedAccount.getFirstName()
        );

        bankLoanForm.setDebtorLastNameKana(
                verifiedAccount.getLastNameKana()
        );

        bankLoanForm.setDebtorFirstNameKana(
                verifiedAccount.getFirstNameKana()
        );

        bankLoanForm.setBirthDate(
                verifiedAccount.getBirthDate()
        );

        bankLoanForm.setDebtorName(
                verifiedAccount.getLastName()
                        + " "
                        + verifiedAccount.getFirstName()
        );

        // 金利タイプに対応する正しい金利を取得
        BigDecimal interestRate =
                INTEREST_RATE_OPTIONS.get(
                        bankLoanForm.getInterestType()
                );

        // 存在しない金利タイプへ書き換えられた場合
        if (interestRate == null) {
            log.warn(
                    "event=loan_application_integrity_error "
                            + "field=interestType"
            );

            return "redirect:/bankLoan";
        }

        bankLoanForm.setInterestRate(
                interestRate
        );

        // 二重送信を防止するトークンを発行
        String submissionToken =
                UUID.randomUUID().toString();

        bankLoanForm.setSubmissionToken(
                submissionToken
        );

        session.setAttribute(
                SUBMISSION_TOKEN_KEY,
                submissionToken
        );

        /*
         * 本人情報と借入内容を確認済みデータとして
         * サーバー側に保存
         */
        session.setAttribute(
                CONFIRMATION_FORM_KEY,
                bankLoanForm
        );

        session.removeAttribute(
                CONFIRMATION_ERROR_KEY
        );

        return "redirect:/bankLoanConfirmation";
    }

    @GetMapping("/bankLoanConfirmation")
    public String showConfirmation(
            Model model,
            HttpSession session) {

        BankLoanForm bankLoanForm =
                (BankLoanForm) session.getAttribute(
                        CONFIRMATION_FORM_KEY
                );

        if (bankLoanForm == null) {
            return "redirect:/bankLoan";
        }

        model.addAttribute(
                "bankLoanApplication",
                bankLoanForm
        );

        String errorMessage =
                (String) session.getAttribute(
                        CONFIRMATION_ERROR_KEY
                );

        if (errorMessage != null) {
            model.addAttribute(
                    "validationErrors",
                    Collections.singletonList(
                            errorMessage
                    )
            );

            session.removeAttribute(
                    CONFIRMATION_ERROR_KEY
            );
        }

        return "bankLoanConfirmation";
    }

    @PostMapping("/bankLoanCompletion")
    public String completion(
            @ModelAttribute BankLoanForm bankLoanForm,
            HttpSession session) {

        /*
         * 同じブラウザから2つの送信が同時に来ても、
         * 1つずつ処理する
         */
        synchronized (session) {
            String validToken =
                    (String) session.getAttribute(
                            SUBMISSION_TOKEN_KEY
                    );

            String submittedToken =
                    bankLoanForm.getSubmissionToken();

            // トークンがない、または使用済みなら二重送信
            if (
                    validToken == null
                            || submittedToken == null
                            || !validToken.equals(submittedToken)
            ) {
                log.warn(
                        "event=loan_application_duplicate_submit"
                );

                session.setAttribute(
                        CONFIRMATION_ERROR_KEY,
                        "この申込はすでに受け付けています。"
                );

                return "redirect:/bankLoanConfirmation";
            }

            try {
                BankLoanForm confirmedBankLoanForm =
                        (BankLoanForm) session.getAttribute(
                                CONFIRMATION_FORM_KEY
                        );

                if (confirmedBankLoanForm == null) {
                    log.warn(
                            "event=loan_application_invalid_state"
                    );

                    return "redirect:/bankLoan";
                }

                /*
                 * 完了画面から送信された値ではなく、
                 * サーバーに保存した確認済み情報をDBへ登録
                 */
                applyBankLoanService.applyBankLoan(
                        confirmedBankLoanForm
                );

                // 保存に成功したトークンは再利用できない
                session.removeAttribute(
                        SUBMISSION_TOKEN_KEY
                );

                session.setAttribute(
                        COMPLETED_FORM_KEY,
                        confirmedBankLoanForm
                );

                log.info(
                        "event=loan_application_saved"
                );

                /*
                 * 完了画面を直接返さずGETへ移動する。
                 * これにより再読み込みしてもINSERTされない。
                 */
                return "redirect:/bankLoanCompletion";

            } catch (RuntimeException exception) {
                log.error(
                        "event=loan_application_save_failed",
                        exception
                );

                session.setAttribute(
                        CONFIRMATION_ERROR_KEY,
                        "申込の保存中にエラーが発生しました。"
                );

                return "redirect:/bankLoanConfirmation";
            }
        }
    }

    @GetMapping("/bankLoanCompletion")
    public String showCompletion(
            Model model,
            HttpSession session) {

        BankLoanForm bankLoanForm =
                (BankLoanForm) session.getAttribute(
                        COMPLETED_FORM_KEY
                );

        if (bankLoanForm == null) {
            return "redirect:/bankLoan";
        }

        model.addAttribute(
                "bankLoanApplication",
                bankLoanForm
        );

        return "bankLoanCompletion";
    }

    private void addAccountScreenOptions(
            Model model) {

        model.addAttribute(
                "today",
                LocalDate.now()
        );

        model.addAttribute(
                "branchOptions",
                new String[]{
                        "本店営業部",
                        "福岡支店",
                        "博多支店"
                }
        );

        model.addAttribute(
                "subjectOptions",
                new String[]{
                        "普通預金",
                        "当座預金",
                        "貯蓄預金"
                }
        );
    }
}
