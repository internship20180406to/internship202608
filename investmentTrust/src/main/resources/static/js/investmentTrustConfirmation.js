const submitButton = document.getElementById("submit");

submitButton.addEventListener("click", function(event) {

    const bankSubject =
        document.getElementById("bankSubject").textContent;

    const investmentTrustName =
        document.getElementById("investmentTrustName").textContent;

    const purchaseAmount =
        document.getElementById("purchaseAmount").textContent;

    const message =
        "以下の内容で申し込みます。\n\n" +
        "科目名：" + bankSubject + "\n" +
        "銘柄：" + investmentTrustName + "\n" +
        "購入金額：" + Number(purchaseAmount).toLocaleString() + "円\n\n" +
        "この内容でよろしいですか？";

    const result = confirm(message);

    if (!result) {
        event.preventDefault();
    }
});