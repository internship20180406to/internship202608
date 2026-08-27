package com.example.internship.entity;

import lombok.Data;

/**
 * 金融機関マスタ（bank_master テーブル）の1行を表すクラス。
 *
 * 画面から送られてくる値を受け取る InvestmentTrustForm とは役割が違い、
 * こちらは「DBから読んだ1行を持ち運ぶ入れ物」。
 * そのため入力チェックのアノテーション（@NotBlank など）は付けない。
 *
 * @Data はLombokのアノテーション。getter/setter/toString などを
 * コンパイル時に自動生成してくれるので、自分で書く必要はない。
 */
@Data
public class Bank {

    /** 金融機関コード4桁。先頭の0が意味を持つのでString型で扱う */
    private String bankCode;

    /** 金融機関名 */
    private String bankName;

    /** 金融機関名カナ（半角）。かな検索に使う */
    private String bankKana;
}
