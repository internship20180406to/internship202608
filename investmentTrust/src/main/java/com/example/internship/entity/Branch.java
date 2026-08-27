package com.example.internship.entity;

import lombok.Data;

/**
 * 支店マスタ（branch_master テーブル）の1行を表すクラス。
 *
 * 支店コードは「その銀行の中での通し番号」なので、branchCode だけでは支店を特定できない。
 * 必ず bankCode とセットで扱う（テーブル側も (bankCode, branchCode) の複合主キー）。
 */
@Data
public class Branch {

    /** どの金融機関の支店か */
    private String bankCode;

    /** 支店コード3桁 */
    private String branchCode;

    /** 支店名 */
    private String branchName;

    /** 支店名カナ（半角） */
    private String branchKana;
}
