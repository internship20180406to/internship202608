package com.example.internship.controller;

import com.example.internship.entity.BranchMaster;
import com.example.internship.entity.FundMaster;
import com.example.internship.entity.InstitutionMaster;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.entity.InvestmentTrustOrderView;
import com.example.internship.repository.BranchMasterRepository;
import com.example.internship.repository.FundMasterRepository;
import com.example.internship.repository.InstitutionMasterRepository;
import com.example.internship.repository.InvestmentTrustRepository;
import com.example.internship.service.OrderInvestmentTrustService;
import com.example.internship.service.TradeDateCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
//時間取得のためインポート
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// 投資信託の入力・確認・完了・行員モード一覧、各画面の遷移とデータ受け渡しを担当するController
@Controller
public class InvestmentTrustController {

    @Autowired
    private OrderInvestmentTrustService orderInvestmentTrustService;
    @Autowired
    private InstitutionMasterRepository institutionMasterRepository;
    @Autowired
    private BranchMasterRepository branchMasterRepository;
    @Autowired
    private FundMasterRepository fundMasterRepository;
    @Autowired
    private InvestmentTrustRepository investmentTrustRepository;
    @Autowired
    private TradeDateCalculator tradeDateCalculator;

    // 行員モードの固定ID/パスワード(application.ymlのinvestment-trust.staff.*から注入)
    @Value("${investment-trust.staff.id}")
    private String staffId;
    @Value("${investment-trust.staff.password}")
    private String staffPassword;

    // セッションに認証済みフラグを保存するときのキー名
    private static final String STAFF_SESSION_KEY = "staffAuthenticated";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm");

    // 投資信託の紹介・案内ページを表示する(ログイン等はなく「始める」ボタンから入力画面へ進む)
    @GetMapping("/investmentTrust/intro")
    public String intro(Model model) {
        model.addAttribute("fundList", fundMasterRepository.findAll());
        return "investmentTrustIntro";
    }

    // 入力画面を表示する(選択肢用に金融機関・支店・銘柄マスタをModelへ渡す)
    @GetMapping("/investmentTrust")
    public String showForm(Model model) {
        model.addAttribute("investmentTrustApplication", new InvestmentTrustForm());
        addMasterDataToModel(model);
        return "investmentTrustMain";
    }

    // 確認画面を表示する。購入手数料の確定計算と、コード→名称の解決を行う
    @PostMapping("/investmentTrustConfirmation")
    public String confirmation(@ModelAttribute InvestmentTrustForm investmentTrustForm, Model model) {
        Integer purchaseFee = orderInvestmentTrustService.resolvePurchaseFee(
                investmentTrustForm.getFundCode(), investmentTrustForm.getPurchaseAmount());
        investmentTrustForm.setPurchaseFee(purchaseFee);

        // 投資される金額 = 注文金額(購入金額) - 手数料。確認画面の内訳表示用
        model.addAttribute("investedAmount", investmentTrustForm.getPurchaseAmount() - purchaseFee);

        // 約定日はあくまで確認画面時点の見込み。確定値は申し込み(completion)時の日時で算出し直す
        LocalDateTime previewDatetime = LocalDateTime.now();
        model.addAttribute("tradeDatePreview", tradeDateCalculator.calculate(previewDatetime));

        model.addAttribute("institutionName", resolveInstitutionName(investmentTrustForm.getInstitutionCode()));
        model.addAttribute("branchName", resolveBranchName(investmentTrustForm.getInstitutionCode(), investmentTrustForm.getBranchCode()));
        model.addAttribute("fundName", resolveFundName(investmentTrustForm.getFundCode()));
        model.addAttribute("investmentTrustApplication", investmentTrustForm);
        return "investmentTrustConfirmation";
    }

    // 入力内容を修正するときの処理
    @PostMapping("/investmentTrust")//ボタンが押されたときに呼ばれる
    //editメソッドで確認画面から入力画面に戻る処理
    public String edit(@ModelAttribute InvestmentTrustForm investmentTrustForm,
                       Model model) {

        // 入力されていた内容を入力画面に渡す
        model.addAttribute(
                "investmentTrustApplication",
                investmentTrustForm
        );
        addMasterDataToModel(model);

        // 入力画面に戻る
        return "investmentTrustMain";
    }
    //申し込みボタンが押された時の処理(注文日時・約定日を確定させてDBに保存する)
    @PostMapping("/investmentTrustCompletion")
    public String completion(@ModelAttribute InvestmentTrustForm investmentTrustForm, Model model) {

        // 注文日時を取得
        LocalDateTime now = LocalDateTime.now();//この行が実行された時間を取得
        investmentTrustForm.setOrderDatetime(now);

        orderInvestmentTrustService.orderInvestmentTrust(investmentTrustForm);

        // 注文日時を文字列に変換
        String orderDate = now.format(DATE_FORMATTER);

        // 完了画面に注文日時・約定日を渡す
        model.addAttribute("orderDate", orderDate);
        model.addAttribute("tradeDate", investmentTrustForm.getTradeDate());

        return "investmentTrustCompletion";
    }

    // 行員モードのパスワード入力画面を表示する
    @GetMapping("/investmentTrust/staff/login")
    public String staffLogin() {
        return "investmentTrustStaffLogin";
    }

    // 入力されたID・パスワードを固定値と照合する処理
    @PostMapping("/investmentTrust/staff/login")
    public String staffLoginSubmit(@RequestParam String id, @RequestParam String password, HttpSession session, Model model) {
        // IDとパスワードの両方が一致すればセッションに認証済みフラグを立てて一覧画面へ
        if (staffId.equals(id) && staffPassword.equals(password)) {
            session.setAttribute(STAFF_SESSION_KEY, true);
            return "redirect:/investmentTrust/staff";
        }

        // 一致しなければ同じログイン画面にエラーメッセージを出して差し戻す
        model.addAttribute("loginError", true);
        return "investmentTrustStaffLogin";
    }

    // セッションの認証済みフラグを消して行員モードからログアウトする
    @GetMapping("/investmentTrust/staff/logout")
    public String staffLogout(HttpSession session) {
        session.removeAttribute(STAFF_SESSION_KEY);
        return "redirect:/investmentTrust";
    }

    // 行員モード: 全顧客の注文一覧(閲覧専用)。未ログインならログイン画面へ差し戻す
    @GetMapping("/investmentTrust/staff")
    public String staffList(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute(STAFF_SESSION_KEY))) {
            return "redirect:/investmentTrust/staff/login";
        }

        List<InvestmentTrustOrderView> orders = investmentTrustRepository.findAllOrders();
        model.addAttribute("orders", orders);
        return "investmentTrustStaffList";
    }

    // 入力画面用に金融機関・銘柄・支店の全マスタ一覧をModelにセットする
    private void addMasterDataToModel(Model model) {
        model.addAttribute("institutionList", institutionMasterRepository.findAll());
        model.addAttribute("fundList", fundMasterRepository.findAll());
        model.addAttribute("branchList", branchMasterRepository.findAll());
    }

    // 金融機関コードから名称を解決する(確認画面の表示用)
    private String resolveInstitutionName(String institutionCode) {
        return institutionMasterRepository.findAll().stream()
                .filter(institution -> institution.getInstitutionCode().equals(institutionCode))
                .map(InstitutionMaster::getInstitutionName)
                .findFirst()
                .orElse(institutionCode);
    }

    // 金融機関コード+支店コードから支店名を解決する(確認画面の表示用)
    private String resolveBranchName(String institutionCode, String branchCode) {
        return branchMasterRepository.findAll().stream()
                .filter(branch -> branch.getInstitutionCode().equals(institutionCode) && branch.getBranchCode().equals(branchCode))
                .map(BranchMaster::getBranchName)
                .findFirst()
                .orElse(branchCode);
    }

    // 銘柄コードから銘柄名を解決する(確認画面の表示用)
    private String resolveFundName(String fundCode) {
        Optional<FundMaster> fundMaster = fundMasterRepository.findByCode(fundCode);
        return fundMaster.map(FundMaster::getFundName).orElse(fundCode);
    }

}
