package com.example.internship.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 投資信託の申込フォーム。
 * 各項目に付けたアノテーションがサーバサイドの入力チェック（Bean Validation）になる。
 * フロント側（inputConfirmation.js）にも同じ条件のチェックを実装しているが、
 * ブラウザの開発者ツールなどでJSを回避できるため、最終的な判定は必ずこちらで行う。
 */
@Data
public class InvestmentTrustForm {

    //  @NotBlank:String型に作用。null, 空文字, 空白のみをすべて拒否
    @NotBlank(message = "金融機関名を選択してください。")
    private String bankName;

    //  ===== 口座番号：String型 + 書式チェック =====================================
    //  口座番号は「計算に使わない番号」なのでString型で持つ。
    //  Integer型だと先頭の0が消えてしまい、0001234 が 1234 になってしまうため。
    //
    //  @Pattern:値が正規表現に一致するかを判定する。String型の書式チェックはこれを使う。
    //      ^      … 文字列の先頭
    //      [0-9]  … 半角数字1文字（[a-z]なら英小文字、[ｦ-ﾟ]なら半角カナ）
    //      {7}    … 直前の指定（[0-9]）をちょうど7回くり返す
    //      $      … 文字列の末尾
    //    ^ と $ は必須。無いと「どこかに7桁の数字が含まれていればOK」という意味になり、
    //    abc1234567xyz のような値まで通ってしまう。
    //
    //  ※@Pattern は値がnullのときは判定せずOKを返す仕様。未入力の判定は @NotBlank の担当なので、
    //    この2つは必ずセットで付ける（@Pattern だけだと未入力が素通りする）。
    //  ※String型は型変換に失敗しないので、Integer型のときのような typeMismatch エラーは起きない。
    //    裏を返すと @Pattern を書き忘れると何でも登録できてしまうので注意。
    @NotBlank(message = "口座番号を入力してください。")
    @Pattern(regexp = "^[0-9]{7}$", message = "口座番号は半角数字7桁で入力してください。")
    private String bankAccountNum;

    @NotBlank(message = "支店名を選択してください。")
    private String branchName;

    @NotBlank(message = "科目名を選択してください。")
    private String bankAccountType;

    //  ===== 購入者名：半角カナ＋半角スペースのみ ==================================
    //  @Size:文字数の上限・下限を判定。DBの name 列が varchar(20) なので20文字に合わせている。
    //
    //  @Pattern:半角カタカナと半角スペースだけを許可する。
    //      [\\uFF66-\\uFF9F] … 半角カタカナ。UnicodeのU+FF66(ｦ)〜U+FF9F(ﾟ)に連続して並んでいるので
    //                          範囲でまとめて指定できる。小書き(ｧｨｩ)・長音(ｰ)・濁点(ﾞ)・半濁点(ﾟ)も
    //                          この範囲に含まれる。
    //      \\u0020           … 半角スペース。姓と名の区切りに使えるように許可している。
    //      +                 … 直前の指定を1文字以上くり返す
    //
    //  ※Javaのソースでは backslash を2つ書いて \\uFF66 とする。backslash 1つ + uFF66 と書くと
    //    Javaコンパイラがソースコードのユニコードエスケープとして先に解釈してしまい、
    //    ソース上は見えないまま実際の文字（ｦ）に置き換わってしまう。
    //    2つ書けば正規表現エンジン側にエスケープとして渡るので、ソースが半角英数字だけで済み、
    //    ソースファイルの文字コードの影響を受けない。
    //    （なお backslash 1つ + u は、コメントの中に書いてもコンパイルエラーになるので注意）
    //  ※全角カナ（ヤマダ）や漢字（山田）はここで弾かれる。全角→半角の自動変換は行っていない。
    @NotBlank(message = "購入者名を入力してください。")
    @Size(max = 20, message = "購入者名は20文字以内で入力してください。")
    @Pattern(regexp = "^[\\uFF66-\\uFF9F\\u0020]+$",
            message = "購入者名は半角カナ（半角スペース可）で入力してください。")
    private String name;

    @NotBlank(message = "銘柄を選択してください。")
    private String fundName;

    //  金額は合計や比較の計算対象になるのでInteger型のまま扱う
    //  @NotNull:全型に作用。nullを拒否
    //  @Min/@Max:数値の範囲を判定
    @NotNull(message = "金額を入力してください。")
    @Min(value = 10000, message = "金額は10,000円以上で入力してください。")
    @Max(value = 10000000, message = "金額は10,000,000円以下で入力してください。")
    private Integer money;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNum() {
        return bankAccountNum;
    }

    public void setBankAccountNum(String bankAccountNum) {
        this.bankAccountNum = bankAccountNum;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }
}
