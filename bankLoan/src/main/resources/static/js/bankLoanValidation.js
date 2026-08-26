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
            // エラー表示後、正しく入力されたら表示を更新
            field.addEventListener("input", () => {
                if (field.classList.contains("is-invalid")) {
                    validateField(field);
                }
            });

            field.addEventListener("change", () => {
                if (field.classList.contains("is-invalid")) {
                    validateField(field);
                }
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
    if (field.validity.valueMissing) {
        if (field.tagName === "SELECT") {
            return "選択してください。";
        }

        return "入力してください。";
    }

    if (field.validity.patternMismatch) {
        return field.title
            || "指定された形式で入力してください。";
    }

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