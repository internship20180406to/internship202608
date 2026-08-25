package com.example.internship.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// @Selectableの検証処理
public class SelectableValidator implements ConstraintValidator<Selectable, String> {

    private OptionList optionList;

    @Override
    public void initialize(Selectable selectable) {
        this.optionList = selectable.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 未入力は@NotBlankの担当なのでここでは通す（エラーメッセージが二重に出るのを防ぐ）
        if (value == null || value.isBlank()) {
            return true;
        }
        return optionList.getValues().contains(value);
    }
}
