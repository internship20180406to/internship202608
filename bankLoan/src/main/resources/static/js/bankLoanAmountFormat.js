document.addEventListener("DOMContentLoaded", () => {
    const amountInputs =
        document.querySelectorAll("[data-comma-format]");

    if (amountInputs.length === 0) {
        return;
    }

    // 全角数字を半角数字に変換
    function convertToHalfWidth(value) {
        return value.replace(/[０-９]/g, (character) =>
            String.fromCharCode(
                character.charCodeAt(0) - 0xFEE0
            )
        );
    }

    // カンマなど、数字以外を取り除く
    function removeFormatting(value) {
        return convertToHalfWidth(value)
            .replace(/[^\d]/g, "");
    }

    // 3桁ごとにカンマを付ける
    function addCommas(value) {
        if (value === "") {
            return "";
        }

        return value.replace(
            /\B(?=(\d{3})+(?!\d))/g,
            ","
        );
    }

    // 入力可能範囲を確認
    function validateAmount(input) {
        const rawValue =
            removeFormatting(input.value);

        if (rawValue === "") {
            input.setCustomValidity("");
            return;
        }

        const amount = Number(rawValue);
        const min = Number(input.dataset.min);
        const max = Number(input.dataset.max);

        if (amount < min || amount > max) {
            input.setCustomValidity(
                input.dataset.rangeMessage ||
                "入力可能な範囲を確認してください"
            );
        } else {
            input.setCustomValidity("");
        }
    }

    // カンマ表示へ変換
    function formatAmount(input) {
        let rawValue =
            removeFormatting(input.value);

        // 先頭の不要な0を取り除く
        rawValue = rawValue.replace(/^0+(?=\d)/, "");

        input.value = addCommas(rawValue);
        validateAmount(input);
    }

    amountInputs.forEach((input) => {
        input.addEventListener("input", () => {
            formatAmount(input);
        });

        // 確認画面から戻った場合にもカンマを付ける
        formatAmount(input);
    });

    // この画面に存在するフォームを取得
    const forms = new Set();

    amountInputs.forEach((input) => {
        const form = input.closest("form");

        if (form) {
            forms.add(form);
        }
    });

    forms.forEach((form) => {
        form.addEventListener("submit", () => {
            const formAmountInputs =
                form.querySelectorAll("[data-comma-format]");

            // Controllerにはカンマなしの整数を送信
            formAmountInputs.forEach((input) => {
                input.value =
                    removeFormatting(input.value);
            });

            // JavaScriptで送信が中止された場合は表示を戻す
            setTimeout(() => {
                formAmountInputs.forEach((input) => {
                    formatAmount(input);
                });
            }, 0);
        });

        // クリアボタンにも対応
        form.addEventListener("reset", () => {
            setTimeout(() => {
                const formAmountInputs =
                    form.querySelectorAll(
                        "[data-comma-format]"
                    );

                formAmountInputs.forEach((input) => {
                    formatAmount(input);
                });
            }, 0);
        });
    });
});