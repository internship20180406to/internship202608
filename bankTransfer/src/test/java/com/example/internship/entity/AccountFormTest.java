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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Springを起動せず、Bean Validationだけを直接呼んで検証する。
// DBもWebも使わないので、MySQLが動いていなくても実行できる。
@DisplayName("口座情報（画面3）の入力チェック")
class AccountFormTest {

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

    private AccountForm validForm() {
        AccountForm form = new AccountForm();
        form.setBankAccountType("普通");
        form.setBankAccountNum("1234567");
        form.setName("ﾔﾏﾀﾞ ﾀﾛｳ");
        return form;
    }

    private void assertValid(AccountForm form) {
        assertThat(validator.validate(form)).isEmpty();
    }

    private void assertInvalidOn(AccountForm form, String property) {
        Set<ConstraintViolation<AccountForm>> violations = validator.validate(form);
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

    @ParameterizedTest(name = "科目 \"{0}\" は正常")
    @ValueSource(strings = { "普通", "当座", "貯蓄" })
    void 科目は3つの候補を通す(String type) {
        AccountForm form = validForm();
        form.setBankAccountType(type);
        assertValid(form);
    }

    @ParameterizedTest(name = "科目 \"{0}\" は不正")
    @ValueSource(strings = { "", " ", "定期", "ふつう" })
    void 科目は候補外を弾く(String type) {
        AccountForm form = validForm();
        form.setBankAccountType(type);
        assertInvalidOn(form, "bankAccountType");
    }

    @ParameterizedTest(name = "口座番号 \"{0}\" は不正")
    @ValueSource(strings = { "", "12345678", "abcdefg", "１２３４５６７", "123-456" })
    void 口座番号は半角数字7桁以内だけ通す(String num) {
        AccountForm form = validForm();
        form.setBankAccountNum(num);
        assertInvalidOn(form, "bankAccountNum");
    }

    @ParameterizedTest(name = "口座番号 \"{0}\" は正常")
    @ValueSource(strings = { "1234567", "0001234", "0000000", "1", "1234", "123456" })
    void 七桁以内なら通る(String num) {
        AccountForm form = validForm();
        form.setBankAccountNum(num);
        assertValid(form);
    }

    @ParameterizedTest(name = "\"{0}\" は \"{1}\" になる")
    @CsvSource({ "1234567,1234567", "123456,0123456", "1234,0001234", "1,0000001" })
    @DisplayName("7桁に満たない口座番号は先頭を0で埋める")
    void 先頭を0で埋める(String input, String expected) {
        AccountForm form = validForm();
        form.setBankAccountNum(input);

        assertThat(form.paddedBankAccountNum()).isEqualTo(expected);
    }

    @Test
    @DisplayName("未入力のときは埋めずにそのまま返す。埋めると空欄が0000000になってしまう")
    void 未入力は埋めない() {
        AccountForm form = validForm();
        form.setBankAccountNum("");

        assertThat(form.paddedBankAccountNum()).isEmpty();
    }

    @Test
    @DisplayName("口座番号が未入力のときメッセージは1つだけ（必須と桁数が二重に出ない）")
    void 口座番号の未入力メッセージは重複しない() {
        AccountForm form = validForm();
        form.setBankAccountNum("");
        long count = validator.validate(form).stream()
                .filter(violation -> violation.getPropertyPath().toString().equals("bankAccountNum"))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @ParameterizedTest(name = "口座名義 \"{0}\" は不正")
    @ValueSource(strings = { "", " ", "ヤマダ タロウ", "山田太郎", "yamada", "やまだ" })
    void 口座名義は半角カタカナだけ通す(String name) {
        AccountForm form = validForm();
        form.setName(name);
        assertInvalidOn(form, "name");
    }

    @ParameterizedTest(name = "口座名義 \"{0}\" は正常")
    @ValueSource(strings = { "ﾔﾏﾀﾞ ﾀﾛｳ", "ｳﾞｨｳﾞｨｱﾝ･ﾋﾟｰ", "ｱ" })
    void 半角カタカナの口座名義は通る(String name) {
        AccountForm form = validForm();
        form.setName(name);
        assertValid(form);
    }

    @Test
    @DisplayName("口座名義が20文字を超えると不正（DBのカラム長に合わせている）")
    void 口座名義は20文字まで() {
        AccountForm form = validForm();
        form.setName("ｱ".repeat(21));
        assertInvalidOn(form, "name");

        form.setName("ｱ".repeat(20));
        assertValid(form);
    }
}
