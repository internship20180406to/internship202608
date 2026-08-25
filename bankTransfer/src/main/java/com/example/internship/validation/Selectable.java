package com.example.internship.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 値がOptionListの候補に含まれているかを検証するアノテーション
// 画面を経由せず直接POSTされた場合の不正値を防ぐ
@Documented
@Constraint(validatedBy = SelectableValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Selectable {

    // 照合先の候補リスト
    OptionList value();

    String message() default "選択できない値が指定されています";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
