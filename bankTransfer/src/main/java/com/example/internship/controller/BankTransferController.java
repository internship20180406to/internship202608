package com.example.internship.controller;

import com.example.internship.balance.BalanceRepository;
import com.example.internship.entity.AccountForm;
import com.example.internship.entity.AmountForm;
import com.example.internship.entity.BankTransferInput;
import com.example.internship.entity.PayeeForm;
import com.example.internship.fee.TransferAmount;
import com.example.internship.fee.TransferFee;
import com.example.internship.history.RecentPayee;
import com.example.internship.history.TransferHistoryRepository;
import com.example.internship.payee.Payee;
import com.example.internship.payee.PayeeRepository;
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

    // どの入口から来たかをセッションへ記録するときのキー。
    // ステッパーの段数と「戻る」の行き先が経路によって変わるため
    // 経路が決まるのは入口だけ。途中の画面はこの値を書き換えない
    private static final String ROUTE_KEY = "transferRoute";
    private static final String ROUTE_SAVED = "saved";        // 履歴・登録先から振り込む
    private static final String ROUTE_REGISTER = "register";  // 振込先を登録する

    private final ApplyBankTransferService applyBankTransferService;
    private final BankMasterRepository bankMasterRepository;
    private final BranchMasterRepository branchMasterRepository;
    private final TransferHistoryRepository transferHistoryRepository;
    private final PayeeRepository payeeRepository;
    private final BalanceRepository balanceRepository;
    private final TransferFee transferFee;
    private final CurrentUser currentUser;

    // 依存はコンストラクタで受け取る。final にできるので生成後に差し替わらず、
    // 渡し忘れもコンパイル時に分かる（コンストラクタが1つなら @Autowired は不要）
    public BankTransferController(ApplyBankTransferService applyBankTransferService,
                                  BankMasterRepository bankMasterRepository,
                                  BranchMasterRepository branchMasterRepository,
                                  TransferHistoryRepository transferHistoryRepository,
                                  PayeeRepository payeeRepository,
                                  BalanceRepository balanceRepository,
                                  TransferFee transferFee,
                                  CurrentUser currentUser) {
        this.applyBankTransferService = applyBankTransferService;
        this.bankMasterRepository = bankMasterRepository;
        this.branchMasterRepository = branchMasterRepository;
        this.transferHistoryRepository = transferHistoryRepository;
        this.payeeRepository = payeeRepository;
        this.balanceRepository = balanceRepository;
        this.transferFee = transferFee;
        this.currentUser = currentUser;
    }

    // 振込指定日の入力欄で過去日を選べないようにするための下限値
    // （サーバ側は AmountForm の @FutureOrPresent で検証する）
    @ModelAttribute("today")
    public String today() {
        return LocalDate.now().toString();
    }

    // 履歴から振込の画面にいるか判定
    @ModelAttribute("fromSaved")
    public boolean fromSaved(HttpSession session) {
        return ROUTE_SAVED.equals(session.getAttribute(ROUTE_KEY));
    }

    //登録している口座から画面にいるかどうか判定
    @ModelAttribute("registering")
    public boolean registering(HttpSession session) {
        return ROUTE_REGISTER.equals(session.getAttribute(ROUTE_KEY));
    }

    // 金額画面を出し直す。エラーを見せるのに要るものを揃える
    private String redisplayAmount(Model model, BankTransferInput input, int balance) {
        model.addAttribute("input", input);
        model.addAttribute("balance", balance);
        addFeeRule(model, input);
        return "bankTransferAmount";
    }

    // 金額画面で手数料を示すための前提。段が変わる境目と両方の額を渡し、
    // 打ちながら手数料が変わることをJS側で見せられるようにする
    private void addFeeRule(Model model, BankTransferInput input) {
        model.addAttribute("ownBank", transferFee.isOwnBank(input.getBankCode()));
        model.addAttribute("feeUnder", transferFee.of(input.getBankCode(), 1));
        model.addAttribute("feeOver", transferFee.of(input.getBankCode(), TransferFee.MAX_TRANSFER));
        model.addAttribute("feeThreshold", TransferFee.THRESHOLD);
        model.addAttribute("maxTransfer", TransferFee.MAX_TRANSFER);
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
    // 振込先を選ぶ入口。履歴と登録済みをタブで切り替える1画面。
    // まだ手順に入っていない（ここから通常の振込へも抜けられる）のでステッパーを出さない。
    // 順番を飛ばした人の差し戻し先でもある
    @GetMapping("/bankTransfer")
    public String start(@RequestParam(name = "userId", required = false) String userId,
                        HttpSession session, Model model) {
        // 【ログインを作るまでの仮】?userId=... で利用者を切り替える。
        // 入力途中の内容は前の利用者のものなので捨てる
        if (userId != null && !userId.isBlank()) {
            currentUser.switchTo(session, userId);
            session.removeAttribute(INPUT_SESSION_KEY);
        }
        model.addAttribute("tab", "history");
        model.addAttribute("payees", transferHistoryRepository.findRecent(currentUser.resolve(session)));
        return "bankTransferStart";
    }

    // ============================================================
    // 画面1 金融機関の選択
    // ============================================================

    // 手順を最初から始めるための後始末。入力・経路・発行済みのトークンを全部捨てる。
    //
    // 手順の入口が増えるたびにここを通す。1か所でも忘れると、前の手順で入れた内容が
    // 次の手順に残る（実際、履歴から入れた振込先が「新しい振込先を指定」に残っていた）。
    // 確認画面まで進んでいた場合に備えてトークンも捨てる
    private void clearProgress(HttpSession session) {
        session.removeAttribute(INPUT_SESSION_KEY);
        session.removeAttribute(ROUTE_KEY);
        session.removeAttribute(TRANSFER_TOKEN);
    }

    // 中止。入力途中の内容と経路の記憶を捨てて入口へ戻す。
    // 状態が変わるのでGETではなくPOSTで受ける
    @PostMapping("/bankTransfer/cancel")
    public String cancel(HttpSession session) {
        clearProgress(session);
        return "redirect:/bankTransfer";
    }

    // 新しい振込先を指定して振り込む入口。ここで経路を通常に戻す
    @GetMapping("/bankTransfer/new")
    public String startNewTransfer(HttpSession session) {
        clearProgress(session);
        return "redirect:/bankTransfer/bank";
    }

    @GetMapping("/bankTransfer/bank")
    public String bankSelect(HttpSession session, Model model) {
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
    // 履歴から振込先を選ぶ（画面1〜3を飛ばして金額へ進む）
    // 見えるのも選べるのも、その利用者自身の履歴だけ
    // ============================================================

    @PostMapping("/bankTransfer/history")
    public String selectFromHistory(@RequestParam(name = "bankCode", required = false) String bankCode,
                                    @RequestParam(name = "branchCode", required = false) String branchCode,
                                    @RequestParam(name = "bankAccountType", required = false) String bankAccountType,
                                    @RequestParam(name = "bankAccountNum", required = false) String bankAccountNum,
                                    HttpSession session) {
        // 画面から送られた値は信じず、この利用者の履歴に実在するか引き直す。
        // 他人の振込先を送りつけられても、ここで見つからず弾かれる
        Optional<RecentPayee> found = (bankCode == null || branchCode == null
                || bankAccountType == null || bankAccountNum == null)
                ? Optional.empty()
                : transferHistoryRepository.find(currentUser.resolve(session),
                        bankCode, branchCode, bankAccountType, bankAccountNum);
        if (found.isEmpty()) {
            return "redirect:/bankTransfer";
        }
        RecentPayee payee = found.get();

        // 画面1〜3で埋まるはずの項目が、ここで一度に決まる
        BankTransferInput input = input(session);
        input.setBankCode(payee.bankCode());
        input.setBankName(payee.bankName());
        input.setBranchCode(payee.branchCode());
        input.setBranchName(payee.branchName());
        input.setBankAccountType(payee.bankAccountType());
        input.setBankAccountNum(payee.bankAccountNum());
        input.setName(payee.name());
        // 金額と振込指定日は引き継がない。前回と同じとは限らず、
        // 気づかず前回の額を送ってしまう方が被害が大きい
        input.setMoney(null);
        input.setTransferDateTime(null);

        session.setAttribute(ROUTE_KEY, ROUTE_SAVED);
        return "redirect:/bankTransfer/amount";
    }

    // ============================================================
    // 登録した振込先
    // 見えるのも選べるのも消せるのも、その利用者自身の登録先だけ
    // ============================================================

    @GetMapping("/bankTransfer/payees")
    public String payees(HttpSession session, Model model) {
        model.addAttribute("tab", "payees");
        model.addAttribute("payees", payeeRepository.findAll(currentUser.resolve(session)));
        return "bankTransferStart";
    }

    // 登録先を選んで振り込む。履歴から選んだときと同じ状態を作る
    @PostMapping("/bankTransfer/payees/select")
    public String selectPayee(@RequestParam(name = "id", required = false) Integer id,
                              HttpSession session) {
        Optional<Payee> found = id == null
                ? Optional.empty()
                : payeeRepository.find(currentUser.resolve(session), id);
        if (found.isEmpty()) {
            return "redirect:/bankTransfer/payees";
        }
        Payee payee = found.get();

        BankTransferInput input = input(session);
        input.setBankCode(payee.bankCode());
        input.setBankName(payee.bankName());
        input.setBranchCode(payee.branchCode());
        input.setBranchName(payee.branchName());
        input.setBankAccountType(payee.bankAccountType());
        input.setBankAccountNum(payee.bankAccountNum());
        input.setName(payee.name());
        // 履歴から選んだときと同じく、金額と振込指定日は引き継がない
        input.setMoney(null);
        input.setTransferDateTime(null);

        session.setAttribute(ROUTE_KEY, ROUTE_SAVED);
        return "redirect:/bankTransfer/amount";
    }

    @PostMapping("/bankTransfer/payees/delete")
    public String deletePayee(@RequestParam(name = "id", required = false) Integer id,
                              HttpSession session) {
        // 自分の登録先でなければ1件も消えない。消せたかどうかで画面を変えないので
        // 「他人のものだった」ことも相手には分からない
        if (id != null) {
            payeeRepository.delete(currentUser.resolve(session), id);
        }
        return "redirect:/bankTransfer/payees";
    }

    // ============================================================
    // 振込先の登録
    // 画面1〜3をそのまま使って振込先を入力し、最後に呼び名を付けて登録する。
    // 経路の記憶で分かれるのは「画面3の次にどこへ行くか」だけ
    // ============================================================

    @GetMapping("/bankTransfer/payees/new")
    public String startRegister(HttpSession session) {
        // 振込の途中だった内容が混ざらないように捨ててから始める
        clearProgress(session);
        session.setAttribute(ROUTE_KEY, ROUTE_REGISTER);
        return "redirect:/bankTransfer/bank";
    }

    //金融機関の登録の確認画面を表示
    @GetMapping("/bankTransfer/payee/confirm")
    public String payeeConfirm(HttpSession session, Model model) {
        BankTransferInput input = input(session);
        if (!registering(session) || !input.hasAccount()) {
            return "redirect:/bankTransfer/payees";
        }
        if (!model.containsAttribute("payeeForm")) {
            model.addAttribute("payeeForm", new PayeeForm());
        }
        model.addAttribute("input", input);
        model.addAttribute("alreadyRegistered",
                payeeRepository.exists(currentUser.resolve(session), input));
        return "bankTransferPayeeConfirm";
    }

    @PostMapping("/bankTransfer/payee/confirm")
    public String registerPayee(@Valid @ModelAttribute("payeeForm") PayeeForm payeeForm,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {
        BankTransferInput input = input(session);
        if (!registering(session) || !input.hasAccount()) {
            return "redirect:/bankTransfer/payees";
        }
        String userId = currentUser.resolve(session);
        if (bindingResult.hasErrors()) {
            model.addAttribute("input", input);
            model.addAttribute("alreadyRegistered", payeeRepository.exists(userId, input));
            return "bankTransferPayeeConfirm";
        }
        // 既に登録済みでも行き先は同じ（その相手は一覧に出ている）。
        // 後始末も同じにする。経路を残したまま一覧へ戻すと「登録の途中」の
        // ままになり、次に金額画面へ行こうとしても呼び名の画面へ跳ね返される
        payeeRepository.create(userId, payeeForm.getNickname(), input);
        session.removeAttribute(INPUT_SESSION_KEY);
        session.removeAttribute(ROUTE_KEY);
        return "redirect:/bankTransfer/payees";
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
        input.setBankAccountNum(accountForm.paddedBankAccountNum());
        input.setName(accountForm.getName());
        // 振込先の登録なら、金額は入れずに呼び名を付けて終わる
        return registering(session)
                ? "redirect:/bankTransfer/payee/confirm"
                : "redirect:/bankTransfer/amount";
    }

    // ============================================================
    // 画面4 金額と振込指定日
    // ============================================================

    @GetMapping("/bankTransfer/amount")
    public String amountInput(HttpSession session, Model model) {
        BankTransferInput input = input(session);
        if (registering(session)) {
            return "redirect:/bankTransfer/payee/confirm";
        }
        if (!input.hasAccount()) {
            return "redirect:/bankTransfer";
        }
        AmountForm form = new AmountForm();
        // 戻ってきたときは「入力したときの額」を出す。
        // 手数料を含めていたなら、打った額は振込額＋手数料だった
        if (input.getMoney() != null && input.getFee() != null) {
            form.setMoney(input.isFeeIncluded() ? input.getTotal() : input.getMoney());
        }
        form.setFeeIncluded(input.isFeeIncluded());
        form.setTransferDateTime(input.getTransferDateTime());

        model.addAttribute("input", input);
        model.addAttribute("amountForm", form);
        model.addAttribute("balance", balanceRepository.amountOf(currentUser.resolve(session)));
        addFeeRule(model, input);
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
        int balance = balanceRepository.amountOf(currentUser.resolve(session));
        // 形の検証（桁・必須・上限）が通ってから金額の中身を見る。
        // 桁が不正なときに「残高が不足」と出しても意味が通らない。
        // ここで返しておけば、この先 money が入っていることが型の上でも分かる
        if (bindingResult.hasErrors() || amountForm.getMoney() == null) {
            return redisplayAmount(model, input, balance);
        }
        int entered = amountForm.getMoney();
        TransferAmount amount = TransferAmount.of(entered,
                transferFee.of(input.getBankCode(), entered), amountForm.isFeeIncluded());
        if (amount.money() <= 0) {
            bindingResult.rejectValue("money", "fee.tooSmall",
                    String.format("手数料 %,d 円を含めると振込額が残りません", amount.fee()));
        } else if (amount.total() > balance) {
            bindingResult.rejectValue("money", "balance.short",
                    String.format("残高が不足しています（手数料を含めて %,d 円、残高 %,d 円）",
                            amount.total(), balance));
        }
        if (bindingResult.hasErrors()) {
            return redisplayAmount(model, input, balance);
        }
        input.setMoney(amount.money());
        input.setFee(amount.fee());
        input.setFeeIncluded(amount.feeIncluded());
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
        int balance = balanceRepository.amountOf(currentUser.resolve(session));
        model.addAttribute("balance", balance);
        // 引かれるのは振込額ではなく合計額
        model.addAttribute("balanceAfter", balance - input.getTotal());
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
        // 「本日」を選んだまま確認画面で日付をまたぐと、ここへ来た時点では過去の日付になる。
        // 金額画面の検証は通っているので、記録する直前にもう一度見る
        if (input.getTransferDateTime() != null
                && input.getTransferDateTime().isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("dateExpired",
                    "振込指定日が過去の日付になりました。指定し直してください");
            return "redirect:/bankTransfer/amount";
        }
        String userId = currentUser.resolve(session);
        // 金額画面で残高を確かめてから、ここへ来るまでの間に別の振込が確定している
        // ことがある。引けるかどうかは引く瞬間に決まるので、ここでもう一度見る
        if (!applyBankTransferService.applyBankTransfer(userId, input)) {
            redirectAttributes.addFlashAttribute("balanceShort",
                    String.format("残高が不足しています（残高 %,d 円）",
                            balanceRepository.amountOf(userId)));
            return "redirect:/bankTransfer/amount";
        }
        // 使い終わった入力内容は残さない。
        // 経路（ROUTE_KEY）はここでは消さない。完了画面のステッパーが
        // 「どの経路の何段目か」を出すのに要るので、消すのは完了画面を出したあと
        session.removeAttribute(INPUT_SESSION_KEY);
        // リダイレクトすると内容が失われるため、完了画面で表示する分をflash属性で引き継ぐ
        redirectAttributes.addFlashAttribute(RESULT_NAME, input);
        int balanceAfter = balanceRepository.amountOf(userId);
        redirectAttributes.addFlashAttribute("balanceAfter", balanceAfter);
        redirectAttributes.addFlashAttribute("balance", balanceAfter + input.getTotal());
        return "redirect:/bankTransfer/completion";
    }

    // ============================================================
    // 画面6 完了
    // ============================================================

    @GetMapping("/bankTransfer/completion")
    public String completionView(HttpSession session, Model model) {
        // リロードや直接アクセスではflash属性が無く表示する内容が無いので、入力画面へ戻す
        BankTransferInput result = (BankTransferInput) model.getAttribute(RESULT_NAME);
        if (result == null) {
            return "redirect:/bankTransfer";
        }
        // 経路の記憶はここで役目を終える。ステッパーが読む fromSaved は
        // @ModelAttribute で既に解決済みなので、消すのはこの後で構わない
        session.removeAttribute(ROUTE_KEY);
        // 既に登録済みの相手に「登録する」を出しても押せないだけなので、出さない
        model.addAttribute("alreadyRegistered",
                payeeRepository.exists(currentUser.resolve(session), result));
        return "bankTransferCompletion";
    }

    // 完了画面から、今振り込んだ相手を登録先に加える。
    // 画面から来るのは振込先を決める4項目だけで、中身はこの利用者の履歴から引き直す。
    // 呼び名がまだ無いので、登録の経路に乗せて呼び名の画面へ送る
    @PostMapping("/bankTransfer/completion/register")
    public String registerFromCompletion(@RequestParam(name = "bankCode", required = false) String bankCode,
                                         @RequestParam(name = "branchCode", required = false) String branchCode,
                                         @RequestParam(name = "bankAccountType", required = false) String bankAccountType,
                                         @RequestParam(name = "bankAccountNum", required = false) String bankAccountNum,
                                         HttpSession session) {
        Optional<RecentPayee> found = (bankCode == null || branchCode == null
                || bankAccountType == null || bankAccountNum == null)
                ? Optional.empty()
                : transferHistoryRepository.find(currentUser.resolve(session),
                        bankCode, branchCode, bankAccountType, bankAccountNum);
        if (found.isEmpty()) {
            return "redirect:/bankTransfer";
        }
        RecentPayee payee = found.get();

        BankTransferInput input = input(session);
        input.setBankCode(payee.bankCode());
        input.setBankName(payee.bankName());
        input.setBranchCode(payee.branchCode());
        input.setBranchName(payee.branchName());
        input.setBankAccountType(payee.bankAccountType());
        input.setBankAccountNum(payee.bankAccountNum());
        input.setName(payee.name());
        input.setMoney(null);
        input.setTransferDateTime(null);

        session.setAttribute(ROUTE_KEY, ROUTE_REGISTER);
        return "redirect:/bankTransfer/payee/confirm";
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
