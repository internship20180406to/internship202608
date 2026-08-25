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

@DisplayName("金額と振込指定日（画面4）の入力チェック")
class AmountFormTest {

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

    private AmountForm validForm() {
        AmountForm form = new AmountForm();
        form.setMoney(1000);
        form.setTransferDateTime(LocalDate.now());
        return form;
    }

    private void assertValid(AmountForm form) {
        assertThat(validator.validate(form)).isEmpty();
    }

    private void assertInvalidOn(AmountForm form, String property) {
        Set<ConstraintViolation<AmountForm>> violations = validator.validate(form);
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

    @ParameterizedTest(name = "金額 {0} は不正")
    @ValueSource(ints = { 0, -1, -1000 })
    void 金額は1円以上だけ通す(int money) {
        AmountForm form = validForm();
        form.setMoney(money);
        assertInvalidOn(form, "money");
    }

    @Test
    @DisplayName("金額が未入力なら不正")
    void 金額の未入力() {
        AmountForm form = validForm();
        form.setMoney(null);
        assertInvalidOn(form, "money");
    }

    @Test
    @DisplayName("振込指定日は当日以降だけ通す")
    void 振込指定日は過去を弾く() {
        AmountForm form = validForm();

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
        AmountForm form = validForm();
        form.setTransferDateTime(null);
        assertInvalidOn(form, "transferDateTime");
    }
}
