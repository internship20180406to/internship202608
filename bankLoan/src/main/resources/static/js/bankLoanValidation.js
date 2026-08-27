document.addEventListener("DOMContentLoaded", () => {
    const forms =
        document.querySelectorAll("form.needs-validation");

    forms.forEach((form) => {
        // JavaScriptが動いている場合だけ標準の吹き出しを停止
        form.noValidate = true;

        const fields = Array.from(
            form.querySelectorAll(".form-control")
        ).filter((field) =>
            field.type !== "hidden" && !field.disabled
        );

        fields.forEach((field) => {
            // 入力中にリアルタイムで検証
            field.addEventListener("input", () => {
                validateField(field);
            });

            // プルダウンや日付の変更にも対応
            field.addEventListener("change", () => {
                validateField(field);
            });

            // 空欄のまま入力欄から離れた場合にも表示
            field.addEventListener("blur", () => {
                validateField(field);
            });
        });

        form.addEventListener("submit", (event) => {
            // 「戻る」など検証不要のボタンに対応
            if (
                event.submitter
                && event.submitter.hasAttribute("formnovalidate")
            ) {
                return;
            }

            let firstInvalidField = null;

            fields.forEach((field) => {
                const valid = validateField(field);

                if (!valid && firstInvalidField === null) {
                    firstInvalidField = field;
                }
            });

            if (firstInvalidField !== null) {
                event.preventDefault();

                firstInvalidField.scrollIntoView({
                    behavior: "smooth",
                    block: "center"
                });

                setTimeout(() => {
                    firstInvalidField.focus();
                }, 300);
            }
        });

        // クリアを押したときはエラー表示も削除
        form.addEventListener("reset", () => {
            setTimeout(() => {
                fields.forEach((field) => {
                    clearFieldError(field);
                });
            }, 0);
        });
    });
});

function validateField(field) {
    if (field.checkValidity()) {
        clearFieldError(field);
        return true;
    }

    field.classList.add("is-invalid");
    field.setAttribute("aria-invalid", "true");

    let errorMessage = field.errorMessageElement;

    if (!errorMessage) {
        errorMessage = document.createElement("span");
        errorMessage.className = "field-error";

        const fieldKey = field.id || field.name;
        errorMessage.id = fieldKey + "-error";

        const container =
            field.closest(".form-group")
            || field.closest("p")
            || field.parentElement;

        container.appendChild(errorMessage);

        field.setAttribute(
            "aria-describedby",
            errorMessage.id
        );

        field.errorMessageElement = errorMessage;
    }

    errorMessage.textContent =
        getFieldErrorMessage(field);

    return false;
}

function clearFieldError(field) {
    field.classList.remove("is-invalid");
    field.removeAttribute("aria-invalid");
    field.removeAttribute("aria-describedby");

    if (field.errorMessageElement) {
        field.errorMessageElement.remove();
        field.errorMessageElement = null;
    }
}

function getFieldErrorMessage(field) {
    // 必須項目が空欄
    if (field.validity.valueMissing) {
        if (field.dataset.requiredMessage) {
            return field.dataset.requiredMessage;
        }

        if (field.tagName === "SELECT") {
            return "選択してください。";
        }

        return "入力してください。";
    }

    // JavaScript側で設定されたエラー
    if (field.validity.customError) {
        return field.validationMessage;
    }

    // pattern属性に一致しない
    if (field.validity.patternMismatch) {
        return field.dataset.patternMessage
            || field.title
            || "指定された形式で入力してください。";
    }

    // 最小文字数に届かない
    if (field.validity.tooShort) {
        return field.dataset.minlengthMessage
            || "入力文字数が不足しています。";
    }

    // 最大文字数を超えた
    if (field.validity.tooLong) {
        return field.dataset.maxlengthMessage
            || "入力文字数が上限を超えています。";
    }

    // 数値や日付の範囲外
    if (
        field.validity.rangeUnderflow
        || field.validity.rangeOverflow
        || field.validity.stepMismatch
        || field.validity.badInput
    ) {
        return field.title
            || "入力内容を確認してください。";
    }

    return field.title
        || "入力内容を確認してください。";
}