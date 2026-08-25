package com.example.internship.controller;

import com.example.internship.entity.BankTransferForm;
import com.example.internship.service.ApplyBankTransferService;
import com.example.internship.validation.OptionList;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
public class BankTransferController {

    // 二重送信防止用トークンをセッションへ格納するときのキー
    private static final String TRANSFER_TOKEN = "transferToken";

    // 入力内容を画面へ渡すときのキー
    private static final String FORM_NAME = "bankTransferApplication";

    // 入力途中の内容をセッションへ預けるときのキー。
    // 画面をまたいで持ち回るのはブラウザではなくサーバ側の役目にする
    private static final String INPUT_SESSION_KEY = "bankTransferInput";

    @Autowired
    private ApplyBankTransferService applyBankTransferService;

    // 金融機関名の選択肢（各メソッドの実行前に自動でModelへ格納される）
    // 候補の実体はOptionListにあり、入力値の検証（@Selectable）も同じ定義を参照する
    @ModelAttribute("nameOptions")
    public List<String> nameOptions() {
        return OptionList.BANK_NAME.getValues();
    }

    // 科目の選択肢
    @ModelAttribute("accountTypeOptions")
    public List<String> accountTypeOptions() {
        return OptionList.BANK_ACCOUNT_TYPE.getValues();
    }

    // 振込指定日の入力欄で過去日を選べないようにするための下限値（サーバ側は@FutureOrPresentで検証する）
    @ModelAttribute("today")
    public String today() {
        return LocalDate.now().toString();
    }

    // 申し込み入力画面の表示
    @GetMapping("/bankTransfer")
    public String bankTransfer(Model model) {
        model.addAttribute(FORM_NAME, new BankTransferForm());
        return "bankTransferMain";
    }

    // 確認画面の表示（入力値の検証を行う）
    @PostMapping("/bankTransferConfirmation")
    public String confirmation(@Valid @ModelAttribute(FORM_NAME) BankTransferForm bankTransferForm,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {
        //項目に空白がなければ、次の画面に遷移、空白があればもとの画面に遷移
        if (bindingResult.hasErrors()) {
            return "bankTransferMain";
        }
        // 検証を通った入力内容はサーバ側で預かる。画面には持ち回らせない
        session.setAttribute(INPUT_SESSION_KEY, bankTransferForm);
        // 一度きり有効なトークンを発行し、セッションと画面(hidden)の両方に持たせる
        String token = UUID.randomUUID().toString();
        session.setAttribute(TRANSFER_TOKEN, token);
        model.addAttribute(TRANSFER_TOKEN, token);
        return "bankTransferConfirmation";
    }

    // 申し込みの確定（DBへ登録し完了画面へ）
    // 登録するのはセッションに預けた内容だけで、この画面から送られた値は使わない。
    // 検証は確認画面へ進む時点で済んでいるため、ここで再検証する必要はない
    // リロードによる二重登録を防ぐため、登録後はリダイレクトする（PRGパターン）
    // ブラウザバックからの再送信は、トークンを使い捨てにすることで防ぐ
    @PostMapping("/bankTransferCompletion")
    public String completion(@RequestParam(name = TRANSFER_TOKEN, required = false) String transferToken,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        BankTransferForm bankTransferForm = (BankTransferForm) session.getAttribute(INPUT_SESSION_KEY);
        // 照合前に取り出して消すことで、同じトークンでの2回目の登録を通さない
        Object savedToken = session.getAttribute(TRANSFER_TOKEN);
        session.removeAttribute(TRANSFER_TOKEN);

        // 入力内容が無い（セッション切れ）、処理済み、トークンを持たない不正な送信のいずれか
        if (bankTransferForm == null || savedToken == null || !savedToken.equals(transferToken)) {
            return "redirect:/bankTransfer";
        }
        applyBankTransferService.applyBankTransfer(bankTransferForm);
        // 使い終わった入力内容は残さない
        session.removeAttribute(INPUT_SESSION_KEY);
        // リダイレクトすると入力内容が失われるため、完了画面で表示する分をflash属性で引き継ぐ
        // flash属性は1回のリダイレクトでのみ有効で、読み込まれた時点で破棄される
        redirectAttributes.addFlashAttribute(FORM_NAME, bankTransferForm);
        return "redirect:/bankTransferCompletion";
    }

    // 完了画面の表示（リダイレクト先）
    @GetMapping("/bankTransferCompletion")
    public String completionView(Model model) {
        // リロードや直接アクセスではflash属性が無く表示する内容が無いので、入力画面へ戻す
        if (!model.containsAttribute(FORM_NAME)) {
            return "redirect:/bankTransfer";
        }
        return "bankTransferCompletion";
    }
}
