// 投資信託 入力画面(investmentTrustMain.html)の動作を制御するJS
// 支店の絞り込み・手数料プレビュー・高額注文の確認ダイアログを担当する

// 各要素を取得
const institutionSelect = document.getElementById("institutionCode");
const branchSelect = document.getElementById("branchCode");
const fundSelect = document.getElementById("fundCode");
const purchaseAmount = document.getElementById("purchaseAmount");
const feePreview = document.getElementById("feePreview");
const bankAccountNumField = document.getElementById("bankAccountNum");

// フォームを取得
const form = purchaseAmount.closest("form");

// 選択された金融機関コードに紐づく支店だけを支店selectに反映する
function populateBranchOptions(institutionCode, selectedBranchCode) {
    branchSelect.innerHTML = "";

    if (!institutionCode) {
        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "金融機関を選択してください";
        branchSelect.appendChild(placeholder);
        return;
    }

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "選択してください";
    branchSelect.appendChild(placeholder);

    branchMasterList
        .filter(function (branch) { return branch.institutionCode === institutionCode; })
        .forEach(function (branch) {
            const option = document.createElement("option");
            option.value = branch.branchCode;
            option.textContent = branch.branchName;
            if (branch.branchCode === selectedBranchCode) {
                option.selected = true;
            }
            branchSelect.appendChild(option);
        });
}

// カンマ区切り表示から、実際の数値だけを取り出す
function parseAmount(value) {
    return Number(value.replace(/[^0-9]/g, ""));
}

// 購入金額をカンマ区切り表示に整形する(例: 1000000 → 1,000,000)
function formatWithCommas(field) {
    const digits = field.value.replace(/[^0-9]/g, "");
    field.value = digits === "" ? "" : Number(digits).toLocaleString();
}

// 銘柄・購入金額から見込み手数料を計算して表示する(最終確定はサーバー側で再計算する)
function updateFeePreview() {
    const fund = fundMasterList.find(function (f) { return f.fundCode === fundSelect.value; });
    const amount = parseAmount(purchaseAmount.value);

    if (!fund || !amount) {
        feePreview.textContent = "銘柄と購入金額を入力してください";
        return;
    }

    const fee = Math.floor(amount * fund.purchaseFeeRate);
    feePreview.textContent = fee.toLocaleString() + "円(手数料率 " + (fund.purchaseFeeRate * 100).toFixed(1) + "%)";
}

institutionSelect.addEventListener("change", function () {
    populateBranchOptions(institutionSelect.value, null);
});

fundSelect.addEventListener("change", updateFeePreview);

// 購入金額はカンマ区切りに整形しつつ、手数料プレビューも更新する
purchaseAmount.addEventListener("input", function () {
    formatWithCommas(purchaseAmount);
    updateFeePreview();
});

// 口座番号は数字以外の入力を除去し、7桁を超えたら切り捨てる
bankAccountNumField.addEventListener("input", function () {
    bankAccountNumField.value = bankAccountNumField.value.replace(/[^0-9]/g, "").slice(0, 7);
});

// 入力欄から離れたとき、7桁に満たなければ先頭を0で埋める(例: 444 → 0000444)
bankAccountNumField.addEventListener("blur", function () {
    const digits = bankAccountNumField.value;
    if (digits.length > 0 && digits.length < 7) {
        bankAccountNumField.value = digits.padStart(7, "0");
        updateFieldCheck("bankAccountNum", bankAccountNumField);
    }
});

// 必須項目が入力済みになったら「※」を消して緑の「✓」を表示する
const requiredFieldIds = [
    "lastName", "firstName", "address", "contact",
    "institutionCode", "bankAccountNum", "branchCode", "bankSubject",
    "fundCode", "purchaseAmount"
];

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

// すべての必須項目のチェックマークを、今の入力状態に合わせて更新する
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

// 初期表示(修正で入力画面に戻ってきた場合の復元を含む)
populateBranchOptions(institutionSelect.value, initialBranchCode);
formatWithCommas(purchaseAmount);
updateFeePreview();
updateAllFieldChecks();

// 「入力内容をクリア」が押されたとき: ブラウザのリセット処理が終わった直後に
// 支店の選択肢・手数料プレビュー・チェックマークをすべて未入力の状態へ戻す
form.addEventListener("reset", function () {
    setTimeout(function () {
        // institutionSelect自体はブラウザがリセット済みなので、その値に合わせて支店の選択肢を作り直す
        populateBranchOptions(institutionSelect.value, null);
        feePreview.textContent = "銘柄と購入金額を入力してください";
        updateAllFieldChecks();
    }, 0);
});

// 申し込みボタンがクリックされた時の処理　addEventListener(イベント名、実行する関数)
form.addEventListener("submit", function(event) {

    // 購入金額を数字として取得(カンマ区切り表示から数値だけを取り出す)
    const amount = parseAmount(purchaseAmount.value);
    // ↑があるため
    // 百万円以上の場合
    if (amount >= 1000000) {

        // 確認メッセージを表示
        const result = confirm(
            "購入金額が100万円以上です。\n\n" + "購入金額：" + amount.toLocaleString() + "円\n\n" + "この金額でよろしいですか？"
        );

        // キャンセルが押された場合
        if (!result) {

            // 送信ストップ
            event.preventDefault();//eventはsubmitそのものを呼び出すようになっている。preventDefaultはブラウザの動作を止める
            return;
        }
    }

    // 送信する値はカンマなしの数値にしておく(サーバー側はInteger型で受け取るため)
    purchaseAmount.value = String(amount);
});
