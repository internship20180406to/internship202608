package com.example.internship.entity;

import com.example.internship.validation.OptionList;
import com.example.internship.validation.Selectable;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
//入力内容を格納するクラスを定義
// セッションに預ける値なので Serializable にしておく。
// 将来セッションを永続化・複製する構成にしたときに壊れないようにするため
public class BankTransferForm implements Serializable {

    @NotBlank(message = "金融機関名を選択してください")//必須チェックを追加
    @Selectable(value = OptionList.BANK_NAME, message = "金融機関名が正しくありません")
    private String bankName;          // 金融機関名

    @NotBlank(message = "支店名を入力してください")
    @Size(max = 20, message = "支店名は20文字以内で入力してください")//DBのカラム長に合わせる
    private String branchName;        // 支店名

    @NotBlank(message = "科目を選択してください")
    @Selectable(value = OptionList.BANK_ACCOUNT_TYPE, message = "科目が正しくありません")
    private String bankAccountType;   // 科目

    @NotBlank(message = "口座番号を入力してください")
    //DBがchar(7)のため、7桁以外はSQLエラーになる
    //空文字も許容し、未入力のときに@NotBlankとメッセージが二重に出ないようにする
    @Pattern(regexp = "([0-9]{7})?", message = "口座番号は7桁の半角数字で入力してください")
    private String bankAccountNum;    // 口座番号（型をInteger=>Stringに変更）

    @NotBlank(message = "口座名義を入力してください")
    @Size(max = 20, message = "口座名義は20文字以内で入力してください")
    //半角カタカナのみ許容する
    //FF65-FF9Fは 中黒(･)・カタカナ(ｦ-ﾝ)・長音符(ｰ)・濁点(ﾞ)・半濁点(ﾟ)、0020は半角空白
    @Pattern(regexp = "[\\uFF65-\\uFF9F\\u0020]*", message = "口座名義は半角カタカナで入力してください")
    private String name;              // 口座名義

    @NotNull(message = "金額を入力してください")
    @Positive(message = "金額は1円以上で入力してください")
    private Integer money;            // 金額

    @NotNull(message = "振込指定日を入力してください")
    @FutureOrPresent(message = "振込指定日に過去の日付は指定できません")//当日は指定可
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transferDateTime;   // 振込指定日
}
