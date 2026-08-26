package com.example.internship.fee;

import org.springframework.stereotype.Component;

// 振込手数料の決まり。
//
// 手数料の段（220/330）は「利用者が入力した額」で決める。
// 相手が受け取る額で決めようとすると、手数料を差し引いた額がもう一段下がって
// 手数料も変わる、という堂々巡りになり、境目に答えの無い金額が生まれる
// （30,220円は 220円でも330円でも辻褄が合わない）。
// 打った額で決めれば、利用者から見ても「この額ならこの手数料」と一定になる。
@Component
public class TransferFee {

    // 自行。このアプリを動かしている銀行あては手数料が無料
    public static final String OWN_BANK_CODE = "0177";

    // 手数料の段が変わる境目
    static final int THRESHOLD = 30_000;

    static final int FEE_UNDER = 220;
    static final int FEE_OVER = 330;

    // 1回の振込で送れる上限
    public static final int MAX_TRANSFER = 2_000_000;

    // 振込先と入力額から手数料を求める
    public int of(String bankCode, int enteredAmount) {
        if (OWN_BANK_CODE.equals(bankCode)) {
            return 0;
        }
        return enteredAmount < THRESHOLD ? FEE_UNDER : FEE_OVER;
    }

    public boolean isOwnBank(String bankCode) {
        return OWN_BANK_CODE.equals(bankCode);
    }
}
