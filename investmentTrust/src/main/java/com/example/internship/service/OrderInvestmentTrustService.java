package com.example.internship.service;

import com.example.internship.entity.FundMaster;
import com.example.internship.entity.InvestmentTrustForm;
import com.example.internship.repository.FundMasterRepository;
import com.example.internship.repository.InvestmentTrustRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

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

    // 銘柄マスタの手数料率から購入手数料(円未満切り捨て)を算出する
    public Integer resolvePurchaseFee(String fundCode, Integer purchaseAmount) {
        FundMaster fundMaster = fundMasterRepository.findByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("未知の銘柄コードです: " + fundCode));

        BigDecimal fee = fundMaster.getPurchaseFeeRate()
                .multiply(BigDecimal.valueOf(purchaseAmount))
                .setScale(0, RoundingMode.DOWN);

        return fee.intValue();
    }

    // 購入手数料と約定日を確定させたうえで、注文をDBに保存する
    public void orderInvestmentTrust(InvestmentTrustForm investmentTrustForm) {
        LocalDateTime orderDatetime = investmentTrustForm.getOrderDatetime();
        investmentTrustForm.setPurchaseFee(
                resolvePurchaseFee(investmentTrustForm.getFundCode(), investmentTrustForm.getPurchaseAmount())
        );
        investmentTrustForm.setTradeDate(tradeDateCalculator.calculate(orderDatetime));

        investmentTrustRepository.create(investmentTrustForm);
    }
}
