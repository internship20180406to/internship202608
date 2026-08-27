// 投資信託 申し込み各ステップ画面(お客様情報・口座情報・注文内容)で共通の
// 「必須項目が入力済みになったら※を消して緑の✓に切り替える」動作を担当するJS。
// 呼び出し元テンプレートは、このscriptタグより前に requiredFieldIds(そのページに存在する必須項目のid配列)を
// インライン宣言しておくこと。存在しない項目(他ステップのhiddenフィールドなど)は自動的にスキップする。

// カンマ区切り表示から、実際の数値だけを取り出す(購入金額の入力済み判定で使用)
function parseAmount(value) {
    return Number(value.replace(/[^0-9]/g, ""));
}

// 口座番号は「7桁ちょうど」、購入金額はカンマを除いた数値が1以上であることを入力済みの条件とする
function isFieldFilled(fieldId, field) {
    if (fieldId === "bankAccountNum") {
        return /^[0-9]{7}$/.test(field.value.trim());
    }
    if (fieldId === "purchaseAmount") {
        return parseAmount(field.value) > 0;
    }
    return field.value !== null && field.value.trim() !== "";
}

// 指定した項目のエラーメッセージ欄(id="error-フィールドID")にメッセージを表示する。
// そのページに該当のエラー欄が無い項目(他ステップの項目など)は何もしない。
function showFieldError(fieldId, message) {
    const errorElement = document.getElementById("error-" + fieldId);
    if (!errorElement) {
        return;
    }
    errorElement.textContent = message;
}

function updateFieldCheck(fieldId, field) {
    const requiredMark = document.getElementById("required-" + fieldId);
    const doneMark = document.getElementById("done-" + fieldId);
    if (!requiredMark || !doneMark) {
        return;
    }

    const filled = isFieldFilled(fieldId, field);
    requiredMark.style.display = filled ? "none" : "inline";
    doneMark.style.display = filled ? "inline" : "none";
}

// このページに存在する必須項目のチェックマークを、今の入力状態に合わせて更新する
function updateAllFieldChecks() {
    requiredFieldIds.forEach(function (fieldId) {
        const field = document.getElementById(fieldId);
        if (field) {
            updateFieldCheck(fieldId, field);
        }
    });
}

requiredFieldIds.forEach(function (fieldId) {
    const field = document.getElementById(fieldId);
    if (!field) {
        return;
    }
    const eventName = field.tagName === "SELECT" ? "change" : "input";
    field.addEventListener(eventName, function () { updateFieldCheck(fieldId, field); });
});

// 初期表示(前のステップに戻ってきた場合の復元を含む)
updateAllFieldChecks();

// 「入力内容をクリア」が押されたとき: ブラウザのリセット処理が終わった直後にチェックマークを未入力の状態へ戻す
const fieldCheckForm = document.querySelector("form");
if (fieldCheckForm) {
    fieldCheckForm.addEventListener("reset", function () {
        setTimeout(updateAllFieldChecks, 0);
    });
}
