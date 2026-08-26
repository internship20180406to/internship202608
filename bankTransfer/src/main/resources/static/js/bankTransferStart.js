// 一覧の絞り込み。行は全部読み込み済みなので、サーバへは問い合わせず手元で隠す。
// JSが動かなければ絞り込めないだけで、一覧そのものは全件見える
document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('payeeFilter');
    const list = document.getElementById('payeeList');
    const noHit = document.getElementById('filterEmpty');
    if (input === null || list === null) {
        return;
    }
    const rows = Array.from(list.children);

    input.addEventListener('input', () => {
        const keyword = input.value.trim().toLowerCase();
        let shown = 0;
        rows.forEach((row) => {
            // 探す対象は data-search に入れてある。行の文字全部を見ると
            // 「削除」や「最終振込日」まで引っかかってしまう
            const hit = keyword === ''
                || (row.dataset.search || '').toLowerCase().indexOf(keyword) >= 0;
            row.hidden = !hit;
            if (hit) {
                shown++;
            }
        });
        if (noHit !== null) {
            noHit.hidden = shown > 0;
        }
    });
});

// 削除の前に一度だけ確かめる。JSが無くても削除は動くので、押し間違いを減らすためだけのもの
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.payee-delete button').forEach((button) => {
        button.addEventListener('click', (event) => {
            if (!confirm('「' + button.dataset.nickname + '」を削除します。よろしいですか？')) {
                event.preventDefault();
            }
        });
    });
});
