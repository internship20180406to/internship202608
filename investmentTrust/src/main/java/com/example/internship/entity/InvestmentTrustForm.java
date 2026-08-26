package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor //定型コード自動生成

// 投資信託注文の入力内容を保持し、入力→確認→完了の各画面間でデータを受け渡すためのForm
public class InvestmentTrustForm {
    @NonNull//HTML状でrequiredを使用
//    金融機関コード
    private String institutionCode;
    @NonNull
//    口座番号(先頭0を保持するためString。7桁ちょうどであることは画面側でチェックする)
    private String bankAccountNum;
//    姓
    @NonNull
    private String lastName;
//    名
    @NonNull
    private String firstName;
//    住所
    @NonNull
    private String address;
//    連絡先
    @NonNull
    private String contact;
//    銘柄コード
    @NonNull
    private String fundCode;
//    科目名
    @NonNull
    private String bankSubject;
//    支店コード
    @NonNull
    private String branchCode;
//    購入金額
    @NonNull
    private Integer purchaseAmount;

//    Integerを使用しているのは、未入力をnull状態で表現するため

//    以下はユーザー入力ではなくサーバー側で算出し、画面間を引き継ぐための値
    private Integer purchaseFee;
    private LocalDateTime orderDatetime;
    private LocalDate tradeDate;
}
