// 投資信託 お客様情報入力画面(investmentTrustMain.html)の動作を制御するJS
// 氏名欄の空白除去、電話番号のハイフン自動整形とリアルタイムバリデーションを担当する

const lastNameField = document.getElementById("lastName");
const firstNameField = document.getElementById("firstName");
const contactField = document.getElementById("contact");

// 姓・名は全角/半角を問わずスペースの誤入力を認めない(前後の余分な空白も含めて自動的に取り除く)
function stripSpaces(field) {
    field.value = field.value.replace(/[\s　]/g, "");
}

[lastNameField, firstNameField].forEach(function (field) {
    field.addEventListener("input", function () { stripSpaces(field); });
});

// 電話番号は数字以外を除去し、090-1234-5678のように3-4-4桁でハイフンを自動挿入する
function formatPhoneNumber(value) {
    const digits = value.replace(/[^0-9]/g, "").slice(0, 11);
    if (digits.length <= 3) {
        return digits;
    }
    if (digits.length <= 7) {
        return digits.slice(0, 3) + "-" + digits.slice(3);
    }
    return digits.slice(0, 3) + "-" + digits.slice(3, 7) + "-" + digits.slice(7);
}

// 入力中は「先頭が0で始まっているか」だけをその場でチェックする(桁数不足はまだ入力途中の可能性があるため見逃す)
function validateContactWhileTyping() {
    const digits = contactField.value.replace(/[^0-9]/g, "");
    if (digits.length > 0 && digits[0] !== "0") {
        showFieldError("contact", "電話番号は0から始まる番号を入力してください");
        return;
    }
    showFieldError("contact", "");
}

// 入力欄から離れたとき、11桁の電話番号として成立しているかを確認する
function validateContactOnBlur() {
    const digits = contactField.value.replace(/[^0-9]/g, "");
    if (digits.length === 0) {
        showFieldError("contact", "");
        return;
    }
    if (digits.length !== 11 || digits[0] !== "0") {
        showFieldError("contact", "電話番号はハイフンを除く11桁の数字で入力してください");
        return;
    }
    showFieldError("contact", "");
}

contactField.addEventListener("input", function () {
    contactField.value = formatPhoneNumber(contactField.value);
    validateContactWhileTyping();
});
contactField.addEventListener("blur", validateContactOnBlur);

// 初期表示(前の画面から戻ってきた場合の復元を含む)
stripSpaces(lastNameField);
stripSpaces(firstNameField);
if (contactField.value) {
    contactField.value = formatPhoneNumber(contactField.value);
}
