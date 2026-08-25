const submitButton = document.getElementById("submit");

submitButton.addEventListener('click', (e) => {
    // confirm の結果（OKなら true、キャンセルなら false）を取得
    const isOk = confirm("操作を実行しますか？");

    // キャンセル（false）が押された場合のみ、フォーム送信を止める
    if (!isOk) {
        e.preventDefault();
    }
});