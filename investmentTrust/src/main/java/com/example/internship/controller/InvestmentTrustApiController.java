package com.example.internship.controller;

import com.example.internship.entity.AccountBalance;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.service.AccountBalanceService;
import com.example.internship.service.InsufficientBalanceException;
import com.example.internship.service.OrderInvestmentTrustService;
import com.example.internship.validation.InvestmentTrustValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * チャット形式の申込画面（investmentTrustChat）が使うAPI。
 *
 * ★なぜ画面用のコントローラと分けるのか:
 *   従来の画面は、検証に失敗すると入力画面のHTMLを丸ごと返している。
 *   チャットUIがそれを受け取ると会話の履歴ごと消えてしまうので、
 *   結果をJSONで返す入口を用意している。
 *
 * ★判定そのものは InvestmentTrustValidator と OrderInvestmentTrustService を
 *   画面と共有している。入口が増えても判定は1か所のまま。
 *
 * ※このアプリには認証が無い。従来の確認画面も、口座番号を入力すれば
 *   その口座の残高を表示していたので、このAPIで新たに増える公開範囲は無い。
 *   実運用するなら、口座の参照は本人確認の内側に置く必要がある。
 */
@Controller
@ResponseBody
public class InvestmentTrustApiController {

    @Autowired
    private AccountBalanceService accountBalanceService;

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;

    @Autowired
    private InvestmentTrustValidator investmentTrustValidator;

    /**
     * 口座を4点セットで照会し、名義と残高を返す。
     * GET /api/accounts?bankCode=0001&branchCode=002&accountType=貯蓄&accountNum=0031111
     *
     * チャットでは口座番号を聞いた直後にこれを呼び、
     * 「ご名義は◯◯様、残高は◯円です」と返している。
     * 従来の画面では確認画面まで進まないと分からなかった情報を、会話の流れで出せる。
     */
    @GetMapping("/api/accounts")
    public ResponseEntity<Map<String, Object>> account(
            @RequestParam String bankCode, @RequestParam String branchCode,
            @RequestParam String accountType, @RequestParam String accountNum) {

        InvestmentTrustForm probe = new InvestmentTrustForm();
        probe.setBankCode(bankCode);
        probe.setBranchCode(branchCode);
        probe.setBankAccountType(accountType);
        probe.setBankAccountNum(accountNum);

        return accountBalanceService.findByForm(probe)
                .map((a) -> ResponseEntity.ok(Map.<String, Object>of(
                        "accountName", a.getAccountName(), "balance", a.getBalance())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 申込を確定する。
     * POST /api/investmentTrust/order
     *
     * 成功: 200 {"ok":true,"bankName":"…","branchName":"…","balanceAfter":9950000}
     * 失敗: 400 {"ok":false,"fieldErrors":{"money":"…"},"globalErrors":["…"]}
     *
     * ★画面版と違いリダイレクトしない（PRGにしない）。
     *   fetchで送るので、ブラウザの再読み込みでPOSTが再送されることがないため。
     *   二重に押されることへの備えは、送信中にボタンを止める形でフロント側が持つ。
     */
    @PostMapping("/api/investmentTrust/order")
    public ResponseEntity<Map<String, Object>> order(
            @Valid @RequestBody InvestmentTrustForm form, BindingResult bindingResult) {

        investmentTrustValidator.validate(form, bindingResult);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(errorBody(bindingResult));
        }
        try {
            long balanceAfter = orderInvestmentTrustService.orderInvestmentTrust(form);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("bankName", form.getBankName());
            body.put("branchName", form.getBranchName());
            body.put("balanceAfter", balanceAfter);
            return ResponseEntity.ok(body);
        } catch (InsufficientBalanceException e) {
            // 会話の途中で残高を確認していても、そこから申込までの間に別の申込で減ることがある。
            // 最終的な判定は引き落としのUPDATE文が行う。
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", false);
            body.put("fieldErrors", Map.of("money", "残高が不足しています。もう一度ご確認ください。"));
            body.put("globalErrors", List.of());
            return ResponseEntity.badRequest().body(body);
        }
    }

    /**
     * BindingResult をJSONにする。
     *
     * 口座が見つからない場合、4項目には「赤枠だけ付ける」ために空文字のエラーが入っている。
     * 空文字はメッセージとして出しても意味がないので、ここでは落として
     * globalErrors 側の1文だけを伝えている。
     */
    private Map<String, Object> errorBody(BindingResult bindingResult) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError e : bindingResult.getFieldErrors()) {
            String message = e.getDefaultMessage();
            if (message != null && !message.isEmpty() && !fieldErrors.containsKey(e.getField())) {
                fieldErrors.put(e.getField(), message);
            }
        }
        List<String> globalErrors = bindingResult.getGlobalErrors().stream()
                .map(ObjectError::getDefaultMessage).toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("fieldErrors", fieldErrors);
        body.put("globalErrors", globalErrors);
        return body;
    }
}
