// 中止の前に一度だけ確かめる。入力した内容が消えるので、押し間違いは痛い。
// JSが無くても中止は動く（確認が出ないだけ）
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('button[form="cancelForm"]').forEach((button) => {
        button.addEventListener('click', (event) => {
            if (!confirm('入力した内容を破棄して最初に戻ります。よろしいですか？')) {
                event.preventDefault();
            }
        });
    });
});
