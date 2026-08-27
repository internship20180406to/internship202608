package com.example.internship.controller;

import com.example.internship.entity.BranchMaster;
import com.example.internship.entity.Customer;
import com.example.internship.entity.CustomerSummary;
import com.example.internship.entity.FundMaster;
import com.example.internship.entity.InstitutionMaster;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.entity.InvestmentTrustOrderView;
import com.example.internship.config.StaffAuthInterceptor;
import com.example.internship.entity.OrderStatusHistoryEntry;
import com.example.internship.repository.BranchMasterRepository;
import com.example.internship.repository.CustomerRepository;
import com.example.internship.repository.FundMasterRepository;
import com.example.internship.repository.GlossaryTermRepository;
import com.example.internship.repository.InstitutionMasterRepository;
import com.example.internship.repository.InvestmentTrustRepository;
import com.example.internship.repository.OrderStatusHistoryRepository;
import com.example.internship.service.OrderInvestmentTrustService;
import com.example.internship.service.OrderStatusService;
import com.example.internship.service.TradeDateCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
//時間取得のためインポート
import java.time.LocalDate;
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
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Autowired
    private OrderStatusService orderStatusService;
    @Autowired
    private GlossaryTermRepository glossaryTermRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 行員モードの固定ID/パスワード(application.ymlのinvestment-trust.staff.*から注入)
    @Value("${investment-trust.staff.id}")
    private String staffId;
    @Value("${investment-trust.staff.password}")
    private String staffPassword;

    // セッションに認証済みフラグを保存するときのキー名(StaffAuthInterceptorと共有する)
    private static final String STAFF_SESSION_KEY = StaffAuthInterceptor.STAFF_SESSION_KEY;
    // お客様モード(注文履歴・状況の確認)で、本人特定済みのcustomer_idをセッションに保存するときのキー名
    private static final String CUSTOMER_SESSION_KEY = "myOrdersCustomerId";
    // 直前に自分がこのセッションで完了させた注文IDを保存するときのキー名。
    // 完了直後はまだお客様モードにログインしていないため、完了画面だけはこれで閲覧を許可する
    private static final String LAST_COMPLETED_ORDER_SESSION_KEY = "lastCompletedOrderId";
    // セッションに申し込み中のフォーム内容を保存するときのキー名
    // 各ステップをPOST→リダイレクト→GETの形にすることで、ブラウザの「戻る」ボタンで
    // フォーム再送信の警告が出ないようにするために使用する
    private static final String APPLICATION_SESSION_KEY = "investmentTrustApplication";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm");

    // 3画面分割の申し込みフローでは、まだ入力していないステップの項目(purchaseAmount)がhiddenで空文字のまま送信される。
    // Integerへの型変換は空文字を許容しないため、空文字はnullとして扱うよう変換方法を登録しておく
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    // セッションに保存された申し込み中のフォームを取得する(未保存ならまっさらなフォームを返す)
    private InvestmentTrustForm getSessionForm(HttpSession session) {
        InvestmentTrustForm form = (InvestmentTrustForm) session.getAttribute(APPLICATION_SESSION_KEY);
        return form != null ? form : new InvestmentTrustForm();
    }

    // Step1(お客様情報・口座情報)を表示する。申し込みの入り口。
    // セッションに途中入力があれば復元し、なければ空のフォームを表示する
    @GetMapping("/investmentTrust")
    public String showForm(HttpSession session, Model model) {
        model.addAttribute("investmentTrustApplication", getSessionForm(session));
        model.addAttribute("institutionList", institutionMasterRepository.findAll());
        model.addAttribute("branchList", branchMasterRepository.findAll());
        return "investmentTrustMain";
    }

    // Step2(注文内容)の「修正内容に戻る」から呼ばれる。セッションに保存してStep1へリダイレクトする。
    // POST後は直接画面を描画せずGETへリダイレクトすることで、ブラウザの「戻る」でのフォーム再送信警告を防ぐ(PRGパターン)
    @PostMapping("/investmentTrust")
    public String saveCustomerInfo(@ModelAttribute InvestmentTrustForm investmentTrustForm, HttpSession session) {
        session.setAttribute(APPLICATION_SESSION_KEY, investmentTrustForm);
        return "redirect:/investmentTrust";
    }

    // Step2(注文内容)を表示する。Step1が未入力のまま直接アクセスされた場合はStep1へ差し戻す
    @GetMapping("/investmentTrust/order")
    public String showOrderInfoForm(HttpSession session, Model model) {
        InvestmentTrustForm investmentTrustForm = getSessionForm(session);
        if (!isStep1Complete(investmentTrustForm)) {
            return "redirect:/investmentTrust";
        }

        model.addAttribute("investmentTrustApplication", investmentTrustForm);
        model.addAttribute("fundList", fundMasterRepository.findAll());
        model.addAttribute("cutoffTime", tradeDateCalculator.getCutoffTime());
        return "investmentTrustOrderInfo";
    }

    // Step1の「次へ」、または確認画面の「修正内容に戻る」から呼ばれる。セッションに保存してStep2へリダイレクトする。
    // お客様モードのログインパスワードは、Step1を離れるこのタイミングでハッシュ化し、以降の画面には平文を残さない
    @PostMapping("/investmentTrust/order")
    public String saveAndShowOrderInfo(@ModelAttribute InvestmentTrustForm investmentTrustForm, HttpSession session) {
        orderInvestmentTrustService.hashPasswordIfPlaintext(investmentTrustForm);
        session.setAttribute(APPLICATION_SESSION_KEY, investmentTrustForm);
        return "redirect:/investmentTrust/order";
    }

    // Step1(お客様情報・口座情報)の必須項目がすべて入力済みかを判定する。
    // 直接URLを叩く等でStep1をスキップされた場合、姓名+連絡先が空のままDBに登録されてしまうのを防ぐためのガード
    private boolean isStep1Complete(InvestmentTrustForm form) {
        return isNotBlank(form.getLastName()) && isNotBlank(form.getFirstName())
                && isNotBlank(form.getAddress()) && isNotBlank(form.getContact()) && isNotBlank(form.getPassword())
                && isNotBlank(form.getInstitutionCode()) && isNotBlank(form.getBankAccountNum())
                && isNotBlank(form.getBranchCode()) && isNotBlank(form.getBankSubject());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // 注文の閲覧権限があるかを判定する: 行員としてログイン済み、
    // またはお客様モード(注文履歴・状況の確認)でこの注文の顧客本人としてログイン済みのいずれか。
    // 単なる連番IDだけで他人の氏名・住所・口座番号等が見えてしまわないようにするためのガード
    private boolean canViewOrder(HttpSession session, Long orderCustomerId) {
        if (Boolean.TRUE.equals(session.getAttribute(STAFF_SESSION_KEY))) {
            return true;
        }
        Object myOrdersCustomerId = session.getAttribute(CUSTOMER_SESSION_KEY);
        return myOrdersCustomerId != null && myOrdersCustomerId.equals(orderCustomerId);
    }

    // 確認画面を表示する。購入手数料の確定計算と、コード→名称の解決を行う。
    // セッション切れ・URL直打ちなどで前段の入力が揃っていない場合は、該当するStepへ差し戻す(purchaseAmountがnullのまま計算するとNPEになるため)
    @GetMapping("/investmentTrustConfirmation")
    public String confirmation(HttpSession session, Model model) {
        InvestmentTrustForm investmentTrustForm = getSessionForm(session);
        if (!isStep1Complete(investmentTrustForm)) {
            return "redirect:/investmentTrust";
        }
        if (investmentTrustForm.getPurchaseAmount() == null || !isNotBlank(investmentTrustForm.getFundCode())) {
            return "redirect:/investmentTrust/order";
        }

        Integer purchaseFee = orderInvestmentTrustService.resolvePurchaseFee(
                investmentTrustForm.getFundCode(), investmentTrustForm.getPurchaseAmount());
        investmentTrustForm.setPurchaseFee(purchaseFee);

        // 投資される金額 = 注文金額(購入金額) - 手数料。確認画面の内訳表示用
        model.addAttribute("investedAmount", investmentTrustForm.getPurchaseAmount() - purchaseFee);

        // 約定日はあくまで確認画面時点の見込み。確定値は申し込み(completion)時の日時で算出し直す
        LocalDateTime previewDatetime = LocalDateTime.now();
        LocalDate tradeDatePreview = tradeDateCalculator.calculate(previewDatetime);
        model.addAttribute("tradeDatePreview", tradeDatePreview);
        // 本日中の約定になるか、翌営業日扱いになるかをお客様・行員双方が確認できるように渡す
        model.addAttribute("sameDayTrade", tradeDateCalculator.isSameDayTrade(previewDatetime, tradeDatePreview));

        model.addAttribute("institutionName", resolveInstitutionName(investmentTrustForm.getInstitutionCode()));
        model.addAttribute("branchName", resolveBranchName(investmentTrustForm.getInstitutionCode(), investmentTrustForm.getBranchCode()));
        model.addAttribute("fundName", resolveFundName(investmentTrustForm.getFundCode()));
        addCostDisclosureToModel(model, investmentTrustForm.getFundCode(), investmentTrustForm.getPurchaseAmount());

        // 確認画面時点での概算購入口数(固定基準価格をもとに計算)
        model.addAttribute("estimatedUnitsPreview",
                orderInvestmentTrustService.resolveEstimatedUnits(investmentTrustForm.getFundCode(), investmentTrustForm.getPurchaseAmount()));

        model.addAttribute("investmentTrustApplication", investmentTrustForm);
        return "investmentTrustConfirmation";
    }

    // Step2の「次へ(確認する)」から呼ばれる。セッションに保存して確認画面へリダイレクトする
    @PostMapping("/investmentTrustConfirmation")
    public String saveAndShowConfirmation(@ModelAttribute InvestmentTrustForm investmentTrustForm, HttpSession session) {
        session.setAttribute(APPLICATION_SESSION_KEY, investmentTrustForm);
        return "redirect:/investmentTrustConfirmation";
    }

    // 完了画面を表示する。DBに保存された注文内容を元に描画するため、リロードしても二重注文にならない。
    // 注文直後(まだお客様モードにログインしていない状態)はセッションに記録したlastCompletedOrderIdで許可し、
    // それ以外は本人(お客様モード)かスタッフとしてログイン済みの場合のみ表示する
    @GetMapping("/investmentTrust/completion/{orderId}")
    public String showCompletion(@PathVariable Long orderId, HttpSession session, Model model) {
        Optional<InvestmentTrustOrderView> orderOptional = investmentTrustRepository.findOrderById(orderId);
        if (orderOptional.isEmpty()) {
            return "redirect:/investmentTrust";
        }
        InvestmentTrustOrderView order = orderOptional.get();

        boolean justCompletedThisOrder = orderId.equals(session.getAttribute(LAST_COMPLETED_ORDER_SESSION_KEY));
        if (!justCompletedThisOrder && !canViewOrder(session, order.getCustomerId())) {
            return "redirect:/investmentTrust";
        }

        model.addAttribute("orderDate", order.getOrderDatetime().format(DATE_FORMATTER));
        model.addAttribute("tradeDate", order.getTradeDate());
        // 本日中の約定になったか、翌営業日扱いになったかをお客様・行員双方が確認できるように渡す
        model.addAttribute("sameDayTrade", tradeDateCalculator.isSameDayTrade(order.getOrderDatetime(), order.getTradeDate()));
        model.addAttribute("estimatedUnits", order.getEstimatedUnits());
        addCostDisclosureToModel(model, order.getFundCode(), order.getPurchaseAmount());

        return "investmentTrustCompletion";
    }

    // 申し込みボタンが押された時の処理(注文日時・約定日を確定させてDBに保存する)。
    // 注文確定はDBへの書き込みを伴うためPOSTのまま維持し、完了後はGETの完了画面へリダイレクトする。
    // これにより、完了画面でブラウザの「戻る」→「進む」やリロードをしても再度注文が作られることはない
    @PostMapping("/investmentTrustCompletion")
    public String completion(@ModelAttribute InvestmentTrustForm investmentTrustForm, HttpSession session) {

        // 注文日時を取得
        LocalDateTime now = LocalDateTime.now();//この行が実行された時間を取得
        investmentTrustForm.setOrderDatetime(now);

        Long orderId = orderInvestmentTrustService.orderInvestmentTrust(investmentTrustForm);

        // 申し込みが完了したので、セッションに保存していた入力内容を破棄する
        session.removeAttribute(APPLICATION_SESSION_KEY);
        // 完了画面はまだお客様モードにログインしていない状態で表示するため、
        // 「自分が今しがた完了させた注文である」ことをセッションに記録して閲覧を許可する
        session.setAttribute(LAST_COMPLETED_ORDER_SESSION_KEY, orderId);

        return "redirect:/investmentTrust/completion/" + orderId;
    }

    // 投資信託が初めてのお客様向けの用語集(閲覧専用、認証不要)
    @GetMapping("/investmentTrust/glossary")
    public String glossary(Model model) {
        model.addAttribute("terms", glossaryTermRepository.findAll());
        return "investmentTrustGlossary";
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

    // 行員モード: 全顧客の注文一覧(閲覧専用)。未ログインアクセスはStaffAuthInterceptorが差し戻す
    @GetMapping("/investmentTrust/staff")
    public String staffList(Model model) {
        // 表示前に、約定日が到来している注文を約定処理する
        orderStatusService.settleEligibleOrders();

        List<InvestmentTrustOrderView> orders = investmentTrustRepository.findAllOrders();
        model.addAttribute("orders", orders);
        return "investmentTrustStaffList";
    }

    // 行員モード: 注文を「発注済み」に進める。未ログインアクセスはStaffAuthInterceptorが差し戻す
    @PostMapping("/investmentTrust/staff/orders/{id}/place")
    public String staffPlaceOrder(@PathVariable Long id) {
        orderStatusService.placeOrder(id, staffId);
        return "redirect:/investmentTrust/staff";
    }

    // 行員モード: 顧客一覧(申し込み履歴の件数・最終申し込み日時を集計して表示)。未ログインアクセスはStaffAuthInterceptorが差し戻す
    @GetMapping("/investmentTrust/staff/customers")
    public String staffCustomerList(Model model) {
        List<CustomerSummary> customers = customerRepository.findAllCustomers();
        model.addAttribute("customers", customers);
        return "investmentTrustStaffCustomerList";
    }

    // 行員モード: 顧客ごとの申し込み履歴詳細。未ログインアクセスはStaffAuthInterceptorが差し戻す。存在しない顧客IDなら一覧へ差し戻す
    @GetMapping("/investmentTrust/staff/customers/{customerId}")
    public String staffCustomerDetail(@PathVariable Long customerId, Model model) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isEmpty()) {
            return "redirect:/investmentTrust/staff/customers";
        }

        // 表示前に、約定日が到来している注文を約定処理する
        orderStatusService.settleEligibleOrders();

        model.addAttribute("customer", customer.get());
        model.addAttribute("orders", investmentTrustRepository.findOrdersByCustomerId(customerId));
        return "investmentTrustStaffCustomerDetail";
    }

    // 行員モード: 銘柄ごとの手数料率・固定基準価格を閲覧する画面(閲覧専用)。未ログインアクセスはStaffAuthInterceptorが差し戻す
    @GetMapping("/investmentTrust/staff/funds")
    public String staffFunds(Model model) {
        model.addAttribute("fundList", fundMasterRepository.findAll());
        return "investmentTrustStaffFundSettings";
    }

    // 顧客向け: お客様モード(注文履歴・状況の確認)からの一覧、または行員モードの顧客詳細からのみ遷移してくる想定の画面。
    // 連番のIDだけで他人の注文が見えないよう、本人(またはスタッフ)としてログイン済みかを確認する
    @GetMapping("/investmentTrust/orders/{id}/status")
    public String orderStatus(@PathVariable Long id, HttpSession session, Model model) {
        // 表示前に、約定日が到来していて基準価額が登録済みなら約定処理する
        orderStatusService.settleEligibleOrders();

        Optional<InvestmentTrustOrderView> order = investmentTrustRepository.findOrderById(id);
        if (order.isEmpty() || !canViewOrder(session, order.get().getCustomerId())) {
            return "redirect:/investmentTrust";
        }

        List<OrderStatusHistoryEntry> history = orderStatusHistoryRepository.findByOrderId(id);
        Optional<Integer> accruedTrustFee = orderInvestmentTrustService.resolveAccruedTrustFee(order.get());

        model.addAttribute("order", order.get());
        model.addAttribute("history", history);
        model.addAttribute("accruedTrustFee", accruedTrustFee.orElse(null));
        model.addAttribute("sameDayTrade", tradeDateCalculator.isSameDayTrade(order.get().getOrderDatetime(), order.get().getTradeDate()));
        return "investmentTrustOrderStatus";
    }

    // お客様向け: ログイン機構が無いため、申し込み時と同じ姓名+連絡先で本人を特定する照会画面を表示する
    @GetMapping("/investmentTrust/myOrders/login")
    public String myOrdersLogin() {
        return "investmentTrustMyOrdersLogin";
    }

    // 入力された連絡先+パスワードが顧客マスタと一致するか照合する処理。
    // contact単体はユニーク制約が無いため、同じ連絡先の候補全件に対してパスワードのハッシュを照合する
    @PostMapping("/investmentTrust/myOrders/login")
    public String myOrdersLoginSubmit(@RequestParam String contact, @RequestParam String password,
                                       HttpSession session, Model model) {
        Optional<Customer> customer = customerRepository.findAllByContact(contact).stream()
                .filter(c -> c.getPassword() != null && !c.getPassword().isEmpty())
                .filter(c -> passwordEncoder.matches(password, c.getPassword()))
                .findFirst();

        if (customer.isEmpty()) {
            // 一致しなければ同じ照会画面にエラーメッセージを出して差し戻す
            model.addAttribute("loginError", true);
            return "investmentTrustMyOrdersLogin";
        }

        session.setAttribute(CUSTOMER_SESSION_KEY, customer.get().getCustomerId());
        return "redirect:/investmentTrust/myOrders";
    }

    // セッションの本人特定済みフラグを消してお客様モードの照会からログアウトする
    @GetMapping("/investmentTrust/myOrders/logout")
    public String myOrdersLogout(HttpSession session) {
        session.removeAttribute(CUSTOMER_SESSION_KEY);
        return "redirect:/investmentTrust/myOrders/login";
    }

    // お客様向け: 本人の申し込み履歴・現在のステータスを一覧表示する。未照会なら照会画面へ差し戻す
    @GetMapping("/investmentTrust/myOrders")
    public String myOrders(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute(CUSTOMER_SESSION_KEY);
        if (customerId == null) {
            return "redirect:/investmentTrust/myOrders/login";
        }

        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isEmpty()) {
            session.removeAttribute(CUSTOMER_SESSION_KEY);
            return "redirect:/investmentTrust/myOrders/login";
        }

        // 表示前に、約定日が到来している注文を約定処理する
        orderStatusService.settleEligibleOrders();

        model.addAttribute("customer", customer.get());
        model.addAttribute("orders", investmentTrustRepository.findOrdersByCustomerId(customerId));
        return "investmentTrustMyOrders";
    }

    // 確認画面・完了画面用: 信託報酬(年率)・信託財産留保額の参考開示情報をModelにセットする
    // (購入時手数料と違い購入代金からは差し引かれないため、料率と概算額を参考情報として表示するだけ)
    private void addCostDisclosureToModel(Model model, String fundCode, Integer purchaseAmount) {
        FundMaster fundMaster = fundMasterRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("未知の銘柄コードです: " + fundCode));

        model.addAttribute("trustFeeRatePercent", toPercentText(fundMaster.getTrustFeeRate()));
        model.addAttribute("trustFeeEstimate", orderInvestmentTrustService.resolveTrustFeeEstimate(fundCode, purchaseAmount));

        boolean hasRedemptionReserve = fundMaster.getRedemptionReserveRate().compareTo(BigDecimal.ZERO) > 0;
        model.addAttribute("hasRedemptionReserve", hasRedemptionReserve);
        model.addAttribute("redemptionReserveRatePercent", toPercentText(fundMaster.getRedemptionReserveRate()));
        model.addAttribute("redemptionReserveEstimate", orderInvestmentTrustService.resolveRedemptionReserveEstimate(fundCode, purchaseAmount));
    }

    // 料率(例: 0.020)を画面表示用のパーセント文字列(例: "2.0%")に変換する
    private String toPercentText(BigDecimal rate) {
        return rate.movePointRight(2).setScale(1, RoundingMode.HALF_UP) + "%";
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
