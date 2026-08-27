package com.example.internship.service;

import com.example.internship.entity.FundMaster;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.entity.InvestmentTrustOrderView;
import com.example.internship.repository.CustomerRepository;
import com.example.internship.repository.FundMasterRepository;
import com.example.internship.repository.InvestmentTrustRepository;
import com.example.internship.repository.OrderStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;

// 注文確定処理(購入手数料の計算・約定日の計算・DB保存)をまとめて行うService
@Service
@Transactional
public class OrderInvestmentTrustService {
    @Autowired
    private InvestmentTrustRepository investmentTrustRepository;
    @Autowired
    private FundMasterRepository fundMasterRepository;
    @Autowired
    private TradeDateCalculator tradeDateCalculator;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // BCryptハッシュの接頭辞("$2a$"等)
    private static final String BCRYPT_PREFIX = "$2";

    // お客様モード(注文履歴確認)のパスワードをBCryptでハッシュ化する。
    // Step1画面を離れた直後(お客様情報・口座情報入力画面→注文内容画面)に一度だけ呼び出し、
    // 以降の画面(hiddenで引き継ぐ注文内容画面・確認画面、およびセッション)には平文を一切残さない。
    // 既にハッシュ化済みの値(画面を行き来して戻ってきた場合)は再ハッシュしない
    public void hashPasswordIfPlaintext(InvestmentTrustForm investmentTrustForm) {
        String password = investmentTrustForm.getPassword();
        if (password != null && !password.startsWith(BCRYPT_PREFIX)) {
            investmentTrustForm.setPassword(passwordEncoder.encode(password));
        }
    }

    // 銘柄マスタの手数料率から購入手数料(円未満切り捨て)を算出する
    public Integer resolvePurchaseFee(String fundCode, Integer purchaseAmount) {
        return applyRate(fundCode, purchaseAmount, FundMaster::getPurchaseFeeRate);
    }

    // 信託報酬(年率)の概算額を算出する(購入代金からは差し引かれない、確認画面での参考開示用)
    public Integer resolveTrustFeeEstimate(String fundCode, Integer purchaseAmount) {
        return applyRate(fundCode, purchaseAmount, FundMaster::getTrustFeeRate);
    }

    // 信託財産留保額の概算額を算出する(解約時に差し引かれる額の参考開示用)
    public Integer resolveRedemptionReserveEstimate(String fundCode, Integer purchaseAmount) {
        return applyRate(fundCode, purchaseAmount, FundMaster::getRedemptionReserveRate);
    }

    // 銘柄マスタの各種料率を購入金額に乗算する(円未満切り捨て)共通処理
    private Integer applyRate(String fundCode, Integer purchaseAmount, Function<FundMaster, BigDecimal> rateGetter) {
        FundMaster fundMaster = fundMasterRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("未知の銘柄コードです: " + fundCode));

        BigDecimal amount = rateGetter.apply(fundMaster)
                .multiply(BigDecimal.valueOf(purchaseAmount))
                .setScale(0, RoundingMode.DOWN);

        return amount.intValue();
    }

    // 銘柄マスタの固定基準価格をもとに口数を計算する(口数 = 購入金額 × 10000 ÷ 基準価格、口未満切り捨て)
    public Long resolveEstimatedUnits(String fundCode, Integer purchaseAmount) {
        FundMaster fundMaster = fundMasterRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("未知の銘柄コードです: " + fundCode));

        return BigDecimal.valueOf(purchaseAmount)
                .multiply(BigDecimal.valueOf(10000))
                .divide(BigDecimal.valueOf(fundMaster.getReferencePrice()), 0, RoundingMode.DOWN)
                .longValue();
    }

    // 約定済みの保有分について、約定日からの経過日数に応じた信託報酬の概算累計額を計算する(参考表示のみ。残高からの実引き落としは行わない)
    public Optional<Integer> resolveAccruedTrustFee(InvestmentTrustOrderView order) {
        if (order.getConfirmedUnits() == null) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        long elapsedDays = ChronoUnit.DAYS.between(order.getTradeDate(), today);
        if (elapsedDays < 0) {
            elapsedDays = 0;
        }

        FundMaster fundMaster = fundMasterRepository.findByCode(order.getFundCode())
                .orElseThrow(() -> new IllegalArgumentException("未知の銘柄コードです: " + order.getFundCode()));

        // 保有評価額 = 確定口数 × 固定基準価格 ÷ 10000
        BigDecimal holdingValue = BigDecimal.valueOf(order.getConfirmedUnits())
                .multiply(BigDecimal.valueOf(fundMaster.getReferencePrice()))
                .divide(BigDecimal.valueOf(10000), 10, RoundingMode.DOWN);

        // 概算累計額 = 保有評価額 × 信託報酬率 × 経過日数 ÷ 365(円未満切り捨て)
        BigDecimal accrued = holdingValue
                .multiply(fundMaster.getTrustFeeRate())
                .multiply(BigDecimal.valueOf(elapsedDays))
                .divide(BigDecimal.valueOf(365), 0, RoundingMode.DOWN);

        return Optional.of(accrued.intValue());
    }

    // 購入手数料・約定日・概算口数を確定させたうえで、注文をDBに保存する。生成された注文IDを返す
    public Long orderInvestmentTrust(InvestmentTrustForm investmentTrustForm) {
        LocalDateTime orderDatetime = investmentTrustForm.getOrderDatetime();
        investmentTrustForm.setPurchaseFee(
                resolvePurchaseFee(investmentTrustForm.getFundCode(), investmentTrustForm.getPurchaseAmount())
        );
        investmentTrustForm.setTradeDate(tradeDateCalculator.calculate(orderDatetime));
        investmentTrustForm.setEstimatedUnits(
                resolveEstimatedUnits(investmentTrustForm.getFundCode(), investmentTrustForm.getPurchaseAmount())
        );

        // 姓名+連絡先が一致する既存顧客がいればそのIDを再利用し、いなければ新規に顧客を登録する。
        // 通常はStep1を離れた時点で既にハッシュ化済みだが、万一平文のまま渡ってきた場合に備えて念のため確認する
        hashPasswordIfPlaintext(investmentTrustForm);
        Long customerId = customerRepository.resolveOrCreateCustomerId(
                investmentTrustForm.getLastName(),
                investmentTrustForm.getFirstName(),
                investmentTrustForm.getAddress(),
                investmentTrustForm.getContact(),
                investmentTrustForm.getPassword()
        );

        Long orderId = investmentTrustRepository.create(investmentTrustForm, customerId);
        orderStatusHistoryRepository.insert(orderId, "申込受付", "CUSTOMER");
        return orderId;
    }
}
