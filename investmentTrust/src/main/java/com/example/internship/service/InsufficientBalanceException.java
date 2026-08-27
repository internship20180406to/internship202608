package com.example.internship.service;

/**
 * 引き落としができなかったことを表す例外。
 *
 * 原因は「残高が足りない」か「その口座が存在しない」のどちらかだが、
 * 呼び出し側の扱いは同じ（申込を成立させず、入力画面に戻す）なので1つにまとめている。
 *
 * RuntimeException を継承しているのが重要な点。
 * Springの @Transactional は、既定では RuntimeException が投げられたときにだけ
 * トランザクションを巻き戻す。検査例外（Exception）にすると、
 * 途中まで書き込んだ内容がそのまま残ってしまう。
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
