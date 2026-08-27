package com.example.internship.constant;

import java.util.List;

/**
 * 投資信託申込画面の選択肢（プルダウン・ラジオボタン）。
 * 「画面に選択肢を表示する処理」と「送信された値が選択肢に含まれるか確認する処理」の
 * 両方から参照するため、定数として1か所にまとめている。
 */
public final class InvestmentTrustOptions {

    /** 金融機関名 */
    public static final List<String> BANK_NAMES = List.of("山陰共同銀行", "こぶた銀行");

    /** 支店名 */
    public static final List<String> BRANCH_NAMES = List.of("和白支店", "宇佐支店");

    /** 科目名（画面ではラジオボタンで選択させる） */
    public static final List<String> ACCOUNT_TYPES = List.of("普通", "当座", "貯蓄", "その他");

    /** 銘柄名 */
    public static final List<String> FUND_NAMES = List.of("キャピタル１", "キャピタル２");

    /** 定数だけを持つクラスなのでインスタンス化させない */
    private InvestmentTrustOptions() {
    }
}
