package com.example.internship.constant;

import java.util.List;

/**
 * 投資信託申込画面の選択肢（ラジオボタン・プルダウン）。
 * 「画面に選択肢を表示する処理」と「送信された値が選択肢に含まれるか確認する処理」の
 * 両方から参照するため、定数として1か所にまとめている。
 *
 * ※金融機関名・支店名はここには無い。件数が増えても対応できるよう、
 *   bank_master / branch_master テーブルに移し、BankMasterRepository から取得している。
 *   「選択肢が固定で少数なら定数、増減するならマスタテーブル」という使い分け。
 */
public final class InvestmentTrustOptions {

    /** 科目名（画面ではラジオボタンで選択させる） */
    public static final List<String> ACCOUNT_TYPES = List.of("普通", "当座", "貯蓄", "その他");

    /** 銘柄名 */
    public static final List<String> FUND_NAMES = List.of("キャピタル１", "キャピタル２");

    /** 定数だけを持つクラスなのでインスタンス化させない */
    private InvestmentTrustOptions() {
    }
}
