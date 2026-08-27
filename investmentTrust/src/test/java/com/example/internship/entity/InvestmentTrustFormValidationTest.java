package com.example.internship.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * InvestmentTrustForm に付けた入力チェック（Bean Validation）のテスト。
 *
 * Springを起動しないので1件あたり数ミリ秒で終わる。DBも要らない。
 * 「正常なフォームを作って、1項目だけ壊す」という書き方に統一しているので、
 * 各テストは「どの項目を、どう壊したら、何と言われるか」だけを見ればよい。
 *
 * ※ここでテストできるのはアノテーションで書いたチェックだけ。
 *   「マスタに実在するコードか」は InvestmentTrustController の担当なので、
 *   InvestmentTrustControllerTest でテストしている。
 */
class InvestmentTrustFormValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        factory.close();
    }

    /** 全項目が正しく入っているフォーム。各テストはここから1項目だけ壊す */
    private InvestmentTrustForm validForm() {
        InvestmentTrustForm form = new InvestmentTrustForm();
        form.setBankCode("0001");
        form.setBranchCode("002");
        form.setBankAccountType("普通");
        form.setBankAccountNum("1234567");
        form.setName("ﾔﾏﾀﾞ ﾀﾛｳ");
        form.setFundName("キャピタル１");
        form.setMoney(10000);
        return form;
    }

    /** 検証して、出たエラーメッセージだけを取り出す */
    private Set<String> messagesOf(InvestmentTrustForm form) {
        return validator.validate(form).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("全項目が正しければエラーは出ない")
    void 正常系() {
        assertThat(messagesOf(validForm())).isEmpty();
    }

    @Test
    @DisplayName("マスタから入る金融機関名・支店名は、空でもエラーにならない")
    void 名称は未設定でもよい() {
        InvestmentTrustForm form = validForm();
        form.setBankName(null);
        form.setBranchName(null);

        //  名称は画面からの入力ではなくサーバがマスタから引く値なので、
        //  入力チェックの対象にしていない。ここが崩れると
        //  「画面から名称を送らせる」作りに戻ってしまうので、テストで固定しておく。
        assertThat(messagesOf(form)).isEmpty();
    }

    @Test
    @DisplayName("金融機関コードが未入力ならエラー")
    void 金融機関コード未入力() {
        InvestmentTrustForm form = validForm();
        form.setBankCode(null);

        assertThat(messagesOf(form)).containsExactly("金融機関コードを入力してください。");
    }

    @Test
    @DisplayName("金融機関コードが3桁ならエラー")
    void 金融機関コードの桁数不足() {
        InvestmentTrustForm form = validForm();
        form.setBankCode("001");

        assertThat(messagesOf(form)).containsExactly("金融機関コードは半角数字4桁で入力してください。");
    }

    @Test
    @DisplayName("金融機関コードに数字以外が入っていればエラー")
    void 金融機関コードが数字以外() {
        InvestmentTrustForm form = validForm();
        form.setBankCode("00A1");

        assertThat(messagesOf(form)).containsExactly("金融機関コードは半角数字4桁で入力してください。");
    }

    @Test
    @DisplayName("支店コードが未入力ならエラー")
    void 支店コード未入力() {
        InvestmentTrustForm form = validForm();
        form.setBranchCode(null);

        assertThat(messagesOf(form)).containsExactly("支店コードを入力してください。");
    }

    @Test
    @DisplayName("支店コードが4桁ならエラー")
    void 支店コードの桁数超過() {
        InvestmentTrustForm form = validForm();
        form.setBranchCode("0021");

        assertThat(messagesOf(form)).containsExactly("支店コードは半角数字3桁で入力してください。");
    }

    @Test
    @DisplayName("口座番号が6桁ならエラー")
    void 口座番号の桁数不足() {
        InvestmentTrustForm form = validForm();
        form.setBankAccountNum("123456");

        assertThat(messagesOf(form)).containsExactly("口座番号は半角数字7桁で入力してください。");
    }

    @Test
    @DisplayName("口座番号の先頭の0は桁数に数える（0031111は7桁として通る）")
    void 口座番号の先頭ゼロ() {
        InvestmentTrustForm form = validForm();
        form.setBankAccountNum("0031111");

        //  口座番号をString型にしている理由がここ。int型だと 31111 になり5桁扱いになってしまう
        assertThat(messagesOf(form)).isEmpty();
    }

    @Test
    @DisplayName("科目名が未入力ならエラー")
    void 科目名未入力() {
        InvestmentTrustForm form = validForm();
        form.setBankAccountType(null);

        assertThat(messagesOf(form)).containsExactly("科目名を選択してください。");
    }

    @Test
    @DisplayName("購入者名が全角カナならエラー")
    void 購入者名が全角カナ() {
        InvestmentTrustForm form = validForm();
        form.setName("ヤマダ タロウ");

        assertThat(messagesOf(form))
                .containsExactly("購入者名は半角カナ（半角スペース可）で入力してください。");
    }

    @Test
    @DisplayName("購入者名が漢字ならエラー")
    void 購入者名が漢字() {
        InvestmentTrustForm form = validForm();
        form.setName("山田 太郎");

        assertThat(messagesOf(form))
                .containsExactly("購入者名は半角カナ（半角スペース可）で入力してください。");
    }

    @Test
    @DisplayName("購入者名が21文字ならエラー")
    void 購入者名の文字数超過() {
        InvestmentTrustForm form = validForm();
        form.setName("ｱ".repeat(21));

        assertThat(messagesOf(form)).containsExactly("購入者名は20文字以内で入力してください。");
    }

    @Test
    @DisplayName("購入者名がちょうど20文字なら通る")
    void 購入者名の上限ぎりぎり() {
        InvestmentTrustForm form = validForm();
        form.setName("ｱ".repeat(20));

        assertThat(messagesOf(form)).isEmpty();
    }

    @Test
    @DisplayName("銘柄が未入力ならエラー")
    void 銘柄未入力() {
        InvestmentTrustForm form = validForm();
        form.setFundName(null);

        assertThat(messagesOf(form)).containsExactly("銘柄を選択してください。");
    }

    @Test
    @DisplayName("金額が未入力ならエラー")
    void 金額未入力() {
        InvestmentTrustForm form = validForm();
        form.setMoney(null);

        assertThat(messagesOf(form)).containsExactly("金額を入力してください。");
    }

    @Test
    @DisplayName("金額が9,999円ならエラー（下限は10,000円）")
    void 金額が下限未満() {
        InvestmentTrustForm form = validForm();
        form.setMoney(9999);

        assertThat(messagesOf(form)).containsExactly("金額は10,000円以上で入力してください。");
    }

    @Test
    @DisplayName("金額が10,000,001円ならエラー（上限は10,000,000円）")
    void 金額が上限超過() {
        InvestmentTrustForm form = validForm();
        form.setMoney(10000001);

        assertThat(messagesOf(form)).containsExactly("金額は10,000,000円以下で入力してください。");
    }

    @Test
    @DisplayName("複数の項目が同時に不正なら、その分だけエラーが出る")
    void 複数項目のエラー() {
        InvestmentTrustForm form = validForm();
        form.setBankCode(null);
        form.setBranchCode(null);
        form.setMoney(0);

        assertThat(messagesOf(form)).containsExactlyInAnyOrder(
                "金融機関コードを入力してください。",
                "支店コードを入力してください。",
                "金額は10,000円以上で入力してください。");
    }

    @Test
    @DisplayName("空文字だと未入力エラーと書式エラーが両方出る（InitBinderがnullに変換する理由）")
    void 空文字はエラーが二重に出る() {
        InvestmentTrustForm form = validForm();
        form.setBankCode("");

        //  @Pattern は「値がnullなら判定しない」仕様だが、空文字は判定してしまうため
        //  「未入力です」と「4桁で入力してください」が同時に出て分かりにくい。
        //
        //  実際の画面ではこうならない。InvestmentTrustController の @InitBinder が
        //  StringTrimmerEditor(true) で空文字をnullに変換してから検証にかけているため、
        //  未入力エラーだけが出る。この変換を外すとメッセージが二重に出るようになる、
        //  ということをこのテストで固定しておく。
        assertThat(messagesOf(form)).containsExactlyInAnyOrder(
                "金融機関コードを入力してください。",
                "金融機関コードは半角数字4桁で入力してください。");
    }
}
