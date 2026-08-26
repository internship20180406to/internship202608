// 投資信託 確認画面(investmentTrustConfirmation.html)の動作を制御するJS
// 「申し込む」ボタン押下時に、最終確認ダイアログを表示する


const submitButton = document.getElementById("submit");
// 申し込みボタンがクリックすされた時の処理、
submitButton.addEventListener("click", function(event) {
    // 科目名の取得
    const bankSubject =
        document.getElementById("bankSubject").textContent;//idをHTML側で探すtextcontentで画面上に表示している文字を読み取りに行く
    //銘柄名の取得
    const investmentTrustName =
        document.getElementById("investmentTrustName").textContent;
    // 購入金額を取得(カンマ区切り表示から数字だけを取り出す)
    const purchaseAmount =
        document.getElementById("purchaseAmount").textContent.replace(/[^0-9]/g, "");

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