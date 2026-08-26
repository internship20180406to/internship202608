const submitButton = document.getElementById("submit");
// 申し込みボタンがクリックすされた時の処理、
submitButton.addEventListener("click", function(event) {
    // 科目名の取得
    const bankSubject =
        document.getElementById("bankSubject").textContent;//idをHTML側で探すtextcontentで画面上に表示している文字を読み取りに行く
    //銘柄名の取得
    const investmentTrustName =
        document.getElementById("investmentTrustName").textContent;
    // 購入金額を取得
    const purchaseAmount =
        document.getElementById("purchaseAmount").textContent;

    const message =
        "以下の内容で申し込みます。\n\n" +
        "科目名：" + bankSubject + "\n" +
        "銘柄：" + investmentTrustName + "\n" +
        "購入金額：" + Number(purchaseAmount).toLocaleString() + "円\n\n" +
        "この内容でよろしいですか？";

    const result = confirm(message);
    //送信ストップ
    if (!result) {
        event.preventDefault();
    }
});