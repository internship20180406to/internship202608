package com.example.internship.controller;

import com.example.internship.entity.AccountForm;
import com.example.internship.entity.AmountForm;
import com.example.internship.entity.BankTransferInput;
import com.example.internship.master.Bank;
import com.example.internship.master.BankMasterRepository;
import com.example.internship.master.Branch;
import com.example.internship.master.BranchMasterRepository;
import com.example.internship.master.Suggestion;
import com.example.internship.service.ApplyBankTransferService;
import com.example.internship.user.CurrentUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 振込の6画面（金融機関 → 支店 → 口座情報 → 金額 → 確認 → 完了）を受け持つ。
// 入力内容はセッションに溜め、画面には持ち回らせない。
@Controller
public class BankTransferController {

    // 二重送信防止用トークンをセッションへ格納するときのキー
    private static final String TRANSFER_TOKEN = "transferToken";

    // 入力途中の内容をセッションへ預けるときのキー
    private static final String INPUT_SESSION_KEY = "bankTransferInput";

    // 完了画面へ内容を渡すときのキー
    private static final String RESULT_NAME = "bankTransferResult";

    private final ApplyBankTransferService applyBankTransferService;
    private final BankMasterRepository bankMasterRepository;
    private final BranchMasterRepository branchMasterRepository;
    private final CurrentUser currentUser;

    // 依存はコンストラクタで受け取る。final にできるので生成後に差し替わらず、
    // 渡し忘れもコンパイル時に分かる（コンストラクタが1つなら @Autowired は不要）
    public BankTransferController(ApplyBankTransferService applyBankTransferService,
                                  BankMasterRepository bankMasterRepository,
                                  BranchMasterRepository branchMasterRepository,
                                  CurrentUser currentUser) {
        this.applyBankTransferService = applyBankTransferService;
        this.bankMasterRepository = bankMasterRepository;
        this.branchMasterRepository = branchMasterRepository;
        this.currentUser = currentUser;
    }

    // 振込指定日の入力欄で過去日を選べないようにするための下限値
    // （サーバ側は AmountForm の @FutureOrPresent で検証する）
    @ModelAttribute("today")
    public String today() {
        return LocalDate.now().toString();
    }

    // セッションの入力内容を取り出す。無ければ空の器を作る
    private BankTransferInput input(HttpSession session) {
        BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_SESSION_KEY);
        if (input == null) {
            input = new BankTransferInput();
            session.setAttribute(INPUT_SESSION_KEY, input);
        }
        return input;
    }

    // ============================================================
    // 画面1 金融機関の選択
    // ============================================================

    // ここで入力内容を消さないのは、確認画面の「変更」から戻ってきたときに
    // 他の項目まで失わせないため。登録が済んだ時点でセッションは破棄している
    //金融機関選択画面を表示する関数(GET/bankTransferがきたら、セッション情報の保持、DBから金融機関情報の取得をおこない金融機関選択画面へ
    @GetMapping("/bankTransfer")
    public String bankSelect(@RequestParam(name = "userId", required = false) String userId,
                             HttpSession session, Model model) {
        // 【ログインを作るまでの仮】?userId=... で利用者を切り替える。
        // 入力途中の内容は前の利用者のものなので捨てる
        if (userId != null && !userId.isBlank()) {
            currentUser.switchTo(session, userId);
            session.removeAttribute(INPUT_SESSION_KEY);
        }
        model.addAttribute("input", input(session));//戻った時リセットされないようにする
        model.addAttribute("banks", bankMasterRepository.findMajor());//データベースから銀行情報を取得
        return "bankTransferBank";
    }

    //選ばれた銀行を検証してセッションに記録し、支店選択画面へ進ませる
    @PostMapping("/bankTransfer/bank")
    public String selectBank(@RequestParam(name = "bankCode", required = false) String bankCode,
                             HttpSession session) {
        Optional<Bank> bank = bankCode == null ? Optional.empty() : bankMasterRepository.findByCode(bankCode);
        if (bank.isEmpty()) {
            // 一覧に無い金融機関が送られてきた。選び直してもらう
            return "redirect:/bankTransfer";
        }
        BankTransferInput input = input(session);
        input.setBankCode(bank.get().bankCode());
        input.setBankName(bank.get().bankName());
        // 銀行を変えたら支店は選び直しにするために支店情報をnullにする
        input.setBranchCode(null);
        input.setBranchName(null);
        return "redirect:/bankTransfer/branch";
    }

    // ============================================================
    // 画面2 支店の選択
    // ============================================================

    //支店選択画面への橋渡し
    @GetMapping("/bankTransfer/branch")
    public String branchSelect(HttpSession session, Model model) {
        BankTransferInput input = input(session);
        //金融機関名が入っているかチェック
        if (!input.hasBank()) {
            return "redirect:/bankTransfer";
        }
        model.addAttribute("input", input);
        return "bankTransferBranch";
    }

    @PostMapping("/bankTransfer/branch")
    public String selectBranch(@RequestParam(name = "branchCode", required = false) String branchCode,
                               HttpSession session) {
        BankTransferInput input = input(session);
        if (!input.hasBank()) {
            return "redirect:/bankTransfer";
        }
        Optional<Branch> branch = branchCode == null
                ? Optional.empty()
                : branchMasterRepository.find(input.getBankCode(), branchCode);
        if (branch.isEmpty()) {
            // その銀行の支店ではないものが送られてきた
            return "redirect:/bankTransfer/branch";
        }
        input.setBranchCode(branch.get().branchCode());
        input.setBranchName(branch.get().branchName());
        return "redirect:/bankTransfer/account";
    }

    // ============================================================
    // 画面3 口座情報
    // ============================================================

    @GetMapping("/bankTransfer/account")
    public String accountInput(HttpSession session, Model model) {
        BankTransferInput input = input(session);
        if (!input.hasBranch()) {
            return "redirect:/bankTransfer";
        }
        // 戻ってきたときに前の入力を復元する
        AccountForm form = new AccountForm();
        form.setBankAccountType(input.getBankAccountType());
        form.setBankAccountNum(input.getBankAccountNum());
        form.setName(input.getName());

        model.addAttribute("input", input);
        model.addAttribute("accountForm", form);
        return "bankTransferAccount";
    }

    @PostMapping("/bankTransfer/account")
    public String submitAccount(@Valid @ModelAttribute("accountForm") AccountForm accountForm,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {
        BankTransferInput input = input(session);
        if (!input.hasBranch()) {
            return "redirect:/bankTransfer";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("input", input);
            return "bankTransferAccount";
        }
        input.setBankAccountType(accountForm.getBankAccountType());
        input.setBankAccountNum(accountForm.getBankAccountNum());
        input.setName(accountForm.getName());
        return "redirect:/bankTransfer/amount";
    }

    // ============================================================
    // 画面4 金額と振込指定日
    // ============================================================

    @GetMapping("/bankTransfer/amount")
    public String amountInput(HttpSession session, Model model) {
        BankTransferInput input = input(session);
        if (!input.hasAccount()) {
            return "redirect:/bankTransfer";
        }
        AmountForm form = new AmountForm();
        form.setMoney(input.getMoney());
        form.setTransferDateTime(input.getTransferDateTime());

        model.addAttribute("input", input);
        model.addAttribute("amountForm", form);
        return "bankTransferAmount";
    }

    @PostMapping("/bankTransfer/amount")
    public String submitAmount(@Valid @ModelAttribute("amountForm") AmountForm amountForm,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {
        BankTransferInput input = input(session);
        if (!input.hasAccount()) {
            return "redirect:/bankTransfer";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("input", input);
            return "bankTransferAmount";
        }
        input.setMoney(amountForm.getMoney());
        input.setTransferDateTime(amountForm.getTransferDateTime());
        return "redirect:/bankTransfer/confirmation";
    }

    // ============================================================
    // 画面5 確認
    // ============================================================

    @GetMapping("/bankTransfer/confirmation")
    public String confirmation(HttpSession session, Model model) {
        BankTransferInput input = input(session);
        if (!input.hasAmount()) {
            return "redirect:/bankTransfer";
        }
        // 一度きり有効なトークンを発行し、セッションと画面(hidden)の両方に持たせる
        String token = UUID.randomUUID().toString();
        session.setAttribute(TRANSFER_TOKEN, token);
        model.addAttribute(TRANSFER_TOKEN, token);
        model.addAttribute("input", input);
        return "bankTransferConfirmation";
    }

    // ============================================================
    // 申し込みの確定
    // 登録するのはセッションに預けた内容だけで、この画面から送られた値は使わない。
    // リロードによる二重登録を防ぐため、登録後はリダイレクトする（PRGパターン）。
    // ブラウザバックからの再送信は、トークンを使い捨てにすることで防ぐ。
    // ============================================================

    @PostMapping("/bankTransfer/completion")
    public String completion(@RequestParam(name = TRANSFER_TOKEN, required = false) String transferToken,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        BankTransferInput input = (BankTransferInput) session.getAttribute(INPUT_SESSION_KEY);
        // 照合前に取り出して消すことで、同じトークンでの2回目の登録を通さない
        Object savedToken = session.getAttribute(TRANSFER_TOKEN);
        session.removeAttribute(TRANSFER_TOKEN);

        // 入力が揃っていない、処理済み、トークンを持たない不正な送信のいずれか
        if (input == null || !input.hasAmount()
                || savedToken == null || !savedToken.equals(transferToken)) {
            return "redirect:/bankTransfer";
        }
        applyBankTransferService.applyBankTransfer(currentUser.resolve(session), input);
        // 使い終わった入力内容は残さない
        session.removeAttribute(INPUT_SESSION_KEY);
        // リダイレクトすると内容が失われるため、完了画面で表示する分をflash属性で引き継ぐ
        redirectAttributes.addFlashAttribute(RESULT_NAME, input);
        return "redirect:/bankTransfer/completion";
    }

    // ============================================================
    // 画面6 完了
    // ============================================================

    @GetMapping("/bankTransfer/completion")
    public String completionView(Model model) {
        // リロードや直接アクセスではflash属性が無く表示する内容が無いので、入力画面へ戻す
        if (!model.containsAttribute(RESULT_NAME)) {
            return "redirect:/bankTransfer";
        }
        return "bankTransferCompletion";
    }

    // ============================================================
    // 検索の候補を返す（画面には遷移せず、JSONだけを返す）
    // 一覧に出ていない金融機関へは、ここを通してのみ到達できる
    // ============================================================

    @GetMapping("/bankTransfer/api/banks")
    @ResponseBody
    public List<Suggestion> searchBanks(@RequestParam(name = "q", defaultValue = "") String keyword) {
        if (keyword.isBlank()) {
            return List.of();
        }
        return bankMasterRepository.search(keyword).stream().map(Suggestion::of).toList();
    }

    @GetMapping("/bankTransfer/api/branches")
    @ResponseBody
    public List<Suggestion> searchBranches(@RequestParam(name = "q", defaultValue = "") String keyword,
                                       HttpSession session) {
        BankTransferInput input = input(session);
        // 銀行が決まっていなければ支店は探せない。他行の支店が混ざらないようにする
        if (!input.hasBank() || keyword.isBlank()) {
            return List.of();
        }
        return branchMasterRepository.search(input.getBankCode(), keyword).stream().map(Suggestion::of).toList();
    }
}
