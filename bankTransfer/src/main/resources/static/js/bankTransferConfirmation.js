const submitButton = document.getElementById("submit")
    submitButton.addEventListener('click', (e) => {
        //キャンセルボタンを押したときに申し込まないように変更
        if (!confirm("この内容で申し込みます。よろしいですか？")) {
            e.preventDefault();
        }
    })