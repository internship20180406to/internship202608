package com.example.internship.service;

import com.example.internship.entity.FundMaster;
import com.example.internship.entity.InvestmentTrustOrderView;
import com.example.internship.repository.FundMasterRepository;
import com.example.internship.repository.InvestmentTrustRepository;
import com.example.internship.repository.OrderStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// 申込受付→発注済み→約定済みのステータス遷移と、変更履歴(いつ・誰が)の記録をまとめて行うService
// バッチ/スケジューラは導入せず、行員一覧・顧客詳細・ステータス確認画面の表示前に約定判定を都度行う方針
@Service
@Transactional
public class OrderStatusService {
    private static final String SYSTEM_ACTOR = "SYSTEM";

    @Autowired
    private InvestmentTrustRepository investmentTrustRepository;
    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Autowired
    private FundMasterRepository fundMasterRepository;

    // 行員が注文を「発注済み」に進める。履歴に処理した行員IDを記録する
    public void placeOrder(Long orderId, String staffId) {
        investmentTrustRepository.markAsPlaced(orderId);
        orderStatusHistoryRepository.insert(orderId, "発注済み", staffId);
    }

    // 「発注済み」かつ約定日が到来している注文を約定処理する(固定基準価格で確定口数を計算する)
    public void settleEligibleOrders() {
        List<InvestmentTrustOrderView> pending = investmentTrustRepository.findPendingSettlement();

        for (InvestmentTrustOrderView order : pending) {
            FundMaster fundMaster = fundMasterRepository.findByCode(order.getFundCode())
                    .orElseThrow(() -> new IllegalArgumentException("未知の銘柄コードです: " + order.getFundCode()));

            long confirmedUnits = BigDecimal.valueOf(order.getPurchaseAmount())
                    .multiply(BigDecimal.valueOf(10000))
                    .divide(BigDecimal.valueOf(fundMaster.getReferencePrice()), 0, RoundingMode.DOWN)
                    .longValue();

            investmentTrustRepository.settleOrder(order.getId(), confirmedUnits);
            orderStatusHistoryRepository.insert(order.getId(), "約定済み", SYSTEM_ACTOR);
        }
    }
}
