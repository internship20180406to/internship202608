package com.example.internship.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Springを起動せず、Bean Validationだけを直接呼んで検証する。
// DBもWebも使わないので、MySQLが動いていなくても実行できる。
@DisplayName("BankTransferForm の入力チェック")
class BankTransferFormTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void startValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    // すべての項目が正しい状態。各テストではここから1項目だけ壊して使う
    private BankTransferForm validForm() {
        BankTransferForm form = new BankTransferForm();
        form.setBankName("ながれぼし銀行");
        form.setBranchName("本店");
        form.setBankAccountType("普通");
        form.setBankAccountNum("1234567");
        form.setName("ﾔﾏﾀﾞ ﾀﾛｳ");
        form.setMoney(1000);
        form.setTransferDateTime(LocalDate.now());
        return form;
    }

    private void assertValid(BankTransferForm form) {
        assertThat(validator.validate(form)).isEmpty();
    }

    private void assertInvalidOn(BankTransferForm form, String property) {
        Set<ConstraintViolation<BankTransferForm>> violations = validator.validate(form);
        assertThat(violations)
                .as("%s に違反が出ること", property)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(property);
    }

    @Test
    @DisplayName("すべて正しければ違反なし")
    void 正常系() {
        assertValid(validForm());
    }

    // ---------- 金融機関名 ----------

    @ParameterizedTest(name = "金融機関名 \"{0}\" は不正")
    @ValueSource(strings = { "", " ", "あああ銀行", "ながれぼし" })
    void 金融機関名は候補にあるものだけ通す(String bankName) {
        BankTransferForm form = validForm();
        form.setBankName(bankName);
        assertInvalidOn(form, "bankName");
    }

    @ParameterizedTest(name = "金融機関名 \"{0}\" は正常")
    @ValueSource(strings = { "ながれぼし銀行", "そらいろ銀行", "つきのわ銀行", "こもれび銀行", "かぜまち銀行" })
    void 候補にある金融機関名は通る(String bankName) {
        BankTransferForm form = validForm();
        form.setBankName(bankName);
        assertValid(form);
    }

    // ---------- 支店名 ----------

    @Test
    @DisplayName("支店名が20文字を超えると不正（DBのカラム長に合わせている）")
    void 支店名は20文字まで() {
        BankTransferForm form = validForm();
        form.setBranchName("あ".repeat(21));
        assertInvalidOn(form, "branchName");

        form.setBranchName("あ".repeat(20));
        assertValid(form);
    }

    // ---------- 科目 ----------

    @ParameterizedTest(name = "科目 \"{0}\" は不正")
    @ValueSource(strings = { "", "定期", "ふつう" })
    void 科目は候補にあるものだけ通す(String type) {
        BankTransferForm form = validForm();
        form.setBankAccountType(type);
        assertInvalidOn(form, "bankAccountType");
    }

    // ---------- 口座番号 ----------

    @ParameterizedTest(name = "口座番号 \"{0}\" は不正")
    @ValueSource(strings = { "", "123456", "12345678", "abcdefg", "１２３４５６７", "123-456" })
    void 口座番号は半角数字7桁だけ通す(String num) {
        BankTransferForm form = validForm();
        form.setBankAccountNum(num);
        assertInvalidOn(form, "bankAccountNum");
    }

    @ParameterizedTest(name = "口座番号 \"{0}\" は正常")
    @ValueSource(strings = { "1234567", "0001234", "0000000" })
    void 先頭が0でも7桁なら通る(String num) {
        BankTransferForm form = validForm();
        form.setBankAccountNum(num);
        assertValid(form);
    }

    @Test
    @DisplayName("口座番号が未入力のときメッセージは1つだけ（必須と桁数が二重に出ない）")
    void 口座番号の未入力メッセージは重複しない() {
        BankTransferForm form = validForm();
        form.setBankAccountNum("");
        long count = validator.validate(form).stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("bankAccountNum"))
                .count();
        assertThat(count).isEqualTo(1);
    }

    // ---------- 口座名義 ----------

    @ParameterizedTest(name = "口座名義 \"{0}\" は不正")
    @ValueSource(strings = { "", " ", "ヤマダ タロウ", "山田太郎", "yamada", "やまだ" })
    void 口座名義は半角カタカナだけ通す(String name) {
        BankTransferForm form = validForm();
        form.setName(name);
        assertInvalidOn(form, "name");
    }

    @ParameterizedTest(name = "口座名義 \"{0}\" は正常")
    @ValueSource(strings = { "ﾔﾏﾀﾞ ﾀﾛｳ", "ｳﾞｨｳﾞｨｱﾝ･ﾋﾟｰ", "ｱ" })
    void 半角カタカナの口座名義は通る(String name) {
        BankTransferForm form = validForm();
        form.setName(name);
        assertValid(form);
    }

    // ---------- 金額 ----------

    @ParameterizedTest(name = "金額 {0} は不正")
    @ValueSource(ints = { 0, -1, -1000 })
    void 金額は1円以上だけ通す(int money) {
        BankTransferForm form = validForm();
        form.setMoney(money);
        assertInvalidOn(form, "money");
    }

    @Test
    @DisplayName("金額が未入力なら不正")
    void 金額の未入力() {
        BankTransferForm form = validForm();
        form.setMoney(null);
        assertInvalidOn(form, "money");
    }

    // ---------- 振込指定日 ----------

    @Test
    @DisplayName("振込指定日は当日以降だけ通す")
    void 振込指定日は過去を弾く() {
        BankTransferForm form = validForm();

        form.setTransferDateTime(LocalDate.now().minusDays(1));
        assertInvalidOn(form, "transferDateTime");

        form.setTransferDateTime(LocalDate.now());
        assertValid(form);

        form.setTransferDateTime(LocalDate.now().plusDays(1));
        assertValid(form);
    }

    @Test
    @DisplayName("振込指定日が未入力なら不正")
    void 振込指定日の未入力() {
        BankTransferForm form = validForm();
        form.setTransferDateTime(null);
        assertInvalidOn(form, "transferDateTime");
    }
}
