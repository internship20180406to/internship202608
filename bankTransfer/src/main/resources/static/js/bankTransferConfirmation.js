// 申し込みの前に一度だけ確かめる。
// これは念のための確認で、二重登録を実際に防いでいるのはサーバ側のトークン
document.addEventListener('DOMContentLoaded', () => {
    const submitButton = document.getElementById('submit');
    if (submitButton === null) {
        return;
    }

    submitButton.addEventListener('click', (event) => {
        //キャンセルボタンを押したときに申し込まないように変更
        if (!confirm('この内容で申し込みます。よろしいですか？')) {
            event.preventDefault();
        }
    });
});
