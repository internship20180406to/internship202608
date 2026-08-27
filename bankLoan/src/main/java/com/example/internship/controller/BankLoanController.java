package com.example.internship.controller;


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

    @GetMapping("/bankLoanSimulation")
    public String simulation(Model model) {
        model.addAttribute("rateOptions", INTEREST_RATE_OPTIONS);
        return "bankLoanSimulation";
    }

    @GetMapping("/bankLoan")
    public String bankTransfer(
            Model model,
            HttpSession session) {

        // 新しい申込を開始するため、以前の申込状態を削除
        session.removeAttribute(SUBMISSION_TOKEN_KEY);
        session.removeAttribute(CONFIRMATION_FORM_KEY);
        session.removeAttribute(COMPLETED_FORM_KEY);
        session.removeAttribute(CONFIRMATION_ERROR_KEY);

        log.info("event=loan_application_started");

        model.addAttribute(
                "bankLoanApplication",
                new BankLoanForm()
        );

        model.addAttribute("today", LocalDate.now());

        model.addAttribute(
                "nameOptions",
                new String[]{
                        "山陰共同銀行",
                        "カウカウ銀行",
                        "流れ星銀行"
                }
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

        return "bankLoanMain";
    }
    // 確認画面から送られてきた入力内容をbankloanMainへ戻す
    @PostMapping("/bankLoanEdit")
    public String editBasicInformation(
            @ModelAttribute BankLoanForm bankLoanForm,
            Model model) {

        model.addAttribute("bankLoanApplication", bankLoanForm);
        model.addAttribute("today", LocalDate.now());

        model.addAttribute(
                "nameOptions",
                new String[]{"山陰共同銀行", "カウカウ銀行", "流れ星銀行"}
        );

        model.addAttribute(
                "branchOptions",
                new String[]{"本店営業部", "福岡支店", "博多支店"}
        );

        model.addAttribute(
                "subjectOptions",
                new String[]{"普通預金", "当座預金", "貯蓄預金"}
        );

        return "bankLoanMain";
    }

    @PostMapping("/bankLoanDetails")
    public String details(
            @ModelAttribute BankLoanForm bankLoanForm,
            Model model) {

        model.addAttribute("bankLoanApplication", bankLoanForm);
        model.addAttribute("rateOptions", INTEREST_RATE_OPTIONS);

        return "bankLoanDetails";
    }

    @PostMapping("/bankLoanConfirmation")
    public String confirmation(
            @ModelAttribute BankLoanForm bankLoanForm,
            HttpSession session) {

        // 金利タイプに対応する金利を設定
        BigDecimal interestRate =
                INTEREST_RATE_OPTIONS.get(
                        bankLoanForm.getInterestType()
                );

        bankLoanForm.setInterestRate(interestRate);

        // 姓と名を結合
        bankLoanForm.setDebtorName(
                bankLoanForm.getDebtorLastName()
                        + " "
                        + bankLoanForm.getDebtorFirstName()
        );

        // 申込ごとのトークンを発行
        String submissionToken =
                UUID.randomUUID().toString();

        bankLoanForm.setSubmissionToken(
                submissionToken
        );

        session.setAttribute(
                SUBMISSION_TOKEN_KEY,
                submissionToken
        );

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
                applyBankLoanService.applyBankLoan(
                        bankLoanForm
                );

                // 保存に成功したトークンは再利用できない
                session.removeAttribute(
                        SUBMISSION_TOKEN_KEY
                );

                session.setAttribute(
                        COMPLETED_FORM_KEY,
                        bankLoanForm
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

}
