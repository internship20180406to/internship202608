// 投資信託 注文内容入力画面(investmentTrustOrderInfo.html)の動作を制御するJS
// 手数料プレビュー・購入金額の整形・高額注文の確認ダイアログを担当する

const fundSelect = document.getElementById("fundCode");
const purchaseAmount = document.getElementById("purchaseAmount");
const feePreview = document.getElementById("feePreview");
const fundPricePreview = document.getElementById("fundPricePreview");

const form = purchaseAmount.closest("form");

// 選択した銘柄の基準価格を表示する(口数計算に使われる固定の参考価格であることを明記する)
function updateFundPricePreview() {
    const fund = fundMasterList.find(function (f) { return f.fundCode === fundSelect.value; });

    if (!fund) {
        fundPricePreview.textContent = "銘柄を選択すると基準価格が表示されます";
        return;
    }

    fundPricePreview.textContent = "基準価格: " + fund.referencePrice.toLocaleString() + "円(1万口あたり・口数計算用の固定値)";
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

fundSelect.addEventListener("change", function () {
    updateFeePreview();
    updateFundPricePreview();
});

// 購入金額はカンマ区切りに整形しつつ、手数料プレビューも更新する
purchaseAmount.addEventListener("input", function () {
    formatWithCommas(purchaseAmount);
    updateFeePreview();
});

// 初期表示(前の画面から戻ってきた場合の復元を含む)
formatWithCommas(purchaseAmount);
updateFeePreview();
updateFundPricePreview();

// 「次へ(確認する)」ボタンがクリックされた時の処理
form.addEventListener("submit", function (event) {

    // 購入金額を数字として取得(カンマ区切り表示から数値だけを取り出す)
    const amount = parseAmount(purchaseAmount.value);

    // 百万円以上の場合
    if (amount >= 1000000) {

        // 確認メッセージを表示
        const result = confirm(
            "購入金額が100万円以上です。\n\n" + "購入金額：" + amount.toLocaleString() + "円\n\n" + "この金額でよろしいですか？"
        );

        // キャンセルが押された場合
        if (!result) {
            event.preventDefault();
            return;
        }
    }

    // 送信する値はカンマなしの数値にしておく(サーバー側はInteger型で受け取るため)
    purchaseAmount.value = String(amount);
});
