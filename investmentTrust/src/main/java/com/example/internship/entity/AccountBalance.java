package com.example.internship.entity;

import lombok.Data;

/**
 * 口座残高（account_balance テーブル）の1行を表すクラス。
 *
 * 口座は「金融機関コード＋支店コード＋科目＋口座番号」の4点セットで初めて特定できる。
 * 口座番号だけでは一意にならない（別の銀行・別の支店に同じ番号が存在しうる）ため、
 * このクラスも必ず4つをセットで持ち回る。
 */
@Data
public class AccountBalance {

    private String bankCode;

    private String branchCode;

    /** 科目（普通/当座/貯蓄/その他） */
    private String accountType;

    /** 口座番号7桁。先頭の0が意味を持つのでString型 */
    private String accountNum;

    /** 口座名義（半角カナ） */
    private String accountName;

    //  残高はintではなくlongで持つ。
    //  intの上限は約21億で、円単位の残高としては足りなくなる可能性がある。
    //  DB側もBIGINTにしてある。
    private long balance;
}
