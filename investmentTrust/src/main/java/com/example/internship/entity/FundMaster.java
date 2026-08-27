package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
// 銘柄マスタ(fund_master)の1行を表すEntity。購入手数料率を保持する
public class FundMaster {
    private String fundCode;
    private String fundName;
    // 購入金額に乗算する手数料率(例: 0.033 = 3.3%)
    private BigDecimal purchaseFeeRate;
    // 信託報酬率(年率。購入代金からは差し引かれず、確認画面での参考開示用)
    private BigDecimal trustFeeRate;
    // 信託財産留保額率(解約時に差し引かれる率。購入時点では参考開示用)
    private BigDecimal redemptionReserveRate;
    // 口数計算用の固定基準価格(1万口あたり・円)。実際の基準価額のような日次更新は行わない簡易な固定値
    private Integer referencePrice;
}
