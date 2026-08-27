package com.example.internship.controller;

import com.example.internship.entity.BankLoanForm;
import com.example.internship.service.ApplyBankLoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BankLoanController {

    private static final long INACTIVITY_TIMEOUT = 10 * 60 * 1000;
    private static final String LAST_ACTIVITY = "lastActivity";

    @Autowired
    private ApplyBankLoanService applyBankLoanService;

    @ModelAttribute("nameOptions")
    public List<String> nameOptions() {
        return List.of(
                "山陰共同銀行",
                "なないろ銀行",
                "桜中央銀行",
                "みなと未来信用銀行",
                "つばさ中央銀行"
        );
    }

    @GetMapping("/bankLoan")
    public String bankLoan(
            Model model,
            HttpSession session) {

        model.addAttribute(
                "bankLoanApplication",
                new BankLoanForm()
        );

        // 無操作タイマー開始
        session.setAttribute(
                LAST_ACTIVITY,
                System.currentTimeMillis()
        );

        return "bankLoanMain";
    }

    @PostMapping("/bankLoan/sessionActivity")
    @ResponseBody
    public Map<String, Long> sessionActivity(
            HttpSession session) {

        long now = System.currentTimeMillis();

        session.setAttribute(
                LAST_ACTIVITY,
                now
        );

        long expiresAt =
                now + INACTIVITY_TIMEOUT;

        return Map.of(
                "expiresAt",
                expiresAt
        );
    }

    @PostMapping("/bankLoanConfirmation")
    public String confirmation(
            @Valid @ModelAttribute("bankLoanApplication") BankLoanForm bankLoanForm,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {

            return "bankLoanMain";

        }

        return "bankLoanConfirmation";
    }

    @PostMapping("/bankLoanCompletion")
    public String completion(
            @ModelAttribute("bankLoanApplication")
            BankLoanForm bankLoanForm,
            HttpSession session,
            Model model) {

        Long lastActivity =
                (Long) session.getAttribute(
                        LAST_ACTIVITY
                );

        // セッション情報がない
        if (lastActivity == null) {
            return "redirect:/bankLoan?timeout";
        }

        long elapsed =
                System.currentTimeMillis()
                        - lastActivity;

        // 10分以上無操作
        if (elapsed >= INACTIVITY_TIMEOUT) {

            session.removeAttribute(
                    LAST_ACTIVITY
            );

            return "redirect:/bankLoan?timeout";
        }

        // 時間内なのでDB登録
        applyBankLoanService
                .applyBankLoan(bankLoanForm);

        return "bankLoanCompletion";
    }

    @GetMapping("/bankLoan/sessionStatus")
    @ResponseBody
    public Map<String, Object> sessionStatus(
            HttpSession session) {

        Long lastActivity =
                (Long) session.getAttribute(LAST_ACTIVITY);

        if (lastActivity == null) {
            return Map.of(
                    "expired", true,
                    "remainingMillis", 0
            );
        }

        long now = System.currentTimeMillis();

        long remaining =
                INACTIVITY_TIMEOUT
                        - (now - lastActivity);

        boolean expired =
                remaining <= 0;

        return Map.of(
                "expired", expired,
                "remainingMillis",
                Math.max(remaining, 0)
        );
    }

}
