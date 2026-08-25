package com.example.internship.master;

// 金融機関マスタの1行。DBから読むだけで、画面から書き換えることはない
public record Bank(String bankCode, String bankName) {
}
