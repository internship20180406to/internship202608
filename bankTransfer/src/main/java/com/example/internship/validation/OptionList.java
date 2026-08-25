package com.example.internship.validation;

import java.util.List;

// 選択式項目の候補を一元管理する
// 画面のプルダウン生成と入力値の検証の両方がここを参照するため、候補の追加はこのファイルだけで完結する
public enum OptionList {

    BANK_NAME(List.of(
            "ながれぼし銀行",
            "そらいろ銀行",
            "つきのわ銀行",
            "こもれび銀行",
            "かぜまち銀行"
    )),

    BANK_ACCOUNT_TYPE(List.of(
            "普通",
            "当座",
            "貯蓄"
    ));

    private final List<String> values;

    OptionList(List<String> values) {
        this.values = values;
    }

    public List<String> getValues() {
        return values;
    }
}
