// 購入金額を取得
const purchaseAmount =
    document.getElementById("purchaseAmount");

// フォームを取得
const form =
    purchaseAmount.closest("form");

// 申し込みボタンがクリックされた時の処理　addEventListener(イベント名、実行する関数)
form.addEventListener("submit", function(event) {

    // 購入金額を数字として取得
    const amount = Number(purchaseAmount.value);
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
        }
    }
});