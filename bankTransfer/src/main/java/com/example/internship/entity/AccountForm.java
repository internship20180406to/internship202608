package com.example.internship.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 画面3（口座情報）の入力。科目の候補との照合はマスタではなくここで持つ
@Data
public class AccountForm {

    @NotBlank(message = "科目を選択してください")
    @Pattern(regexp = "(普通|当座|貯蓄)?", message = "科目が正しくありません")
    private String bankAccountType;

    @NotBlank(message = "口座番号を入力してください")
    //口座番号は7桁で、足りない分は先頭を0で埋めるのが決まり。
    //利用者に0を打たせる意味がないので7桁以内で受け取り、保存する形は padded() で作る。
    //空文字も許容し、未入力のときに@NotBlankとメッセージが二重に出ないようにする
    @Pattern(regexp = "([0-9]{1,7})?", message = "口座番号は7桁以内の半角数字で入力してください")
    private String bankAccountNum;

    //DBがchar(7)のため、7桁に満たないままだと桁がずれる。
    //入力された数字を右に寄せ、空いた先頭を0で埋める
    public String paddedBankAccountNum() {
        if (bankAccountNum == null || bankAccountNum.isBlank()) {
            return bankAccountNum;
        }
        return "0".repeat(Math.max(0, 7 - bankAccountNum.length())) + bankAccountNum;
    }

    @NotBlank(message = "口座名義を入力してください")
    @Size(max = 20, message = "口座名義は20文字以内で入力してください")
    //半角カタカナのみ許容する
    //FF65-FF9Fは 中黒・カタカナ・長音符・濁点・半濁点、0020は半角空白
    @Pattern(regexp = "[\\uFF65-\\uFF9F\\u0020]*", message = "口座名義は半角カタカナで入力してください")
    private String name;
}
