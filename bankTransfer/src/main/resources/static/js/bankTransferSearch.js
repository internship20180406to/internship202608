// 検索欄に打つたびにサーバへ問い合わせ、入力欄の下に候補を重ねて出す。
// 下の一覧は差し替えないので、一覧は固定されたまま隠れるだけで済む。
document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('searchKeyword');
    const panel = document.getElementById('searchSuggest');
    if (input === null || panel === null) {
        return;
    }

    // 候補の1行。押すと、下の一覧と同じくフォームが送信されて選択が確定する
    const rowHtml = (item) =>
        '<li><button class="picklist-row" type="submit" name="' + panel.dataset.paramName + '"'
        + ' value="' + item.code + '">'
        + '<span class="picklist-code">' + item.code + '</span>'
        + '<span>' + item.name + '</span>'
        + '<span class="picklist-chevron" aria-hidden="true">' + String.fromCharCode(8250) + '</span>'
        + '</button></li>';

    const close = () => {
        panel.hidden = true;
        panel.innerHTML = '';
    };

    // 打つたびに投げるので、最後の応答だけを採用する。
    // 通信の速さは順番どおりとは限らず、古い応答が後から届くと表示が巻き戻る
    let latest = 0;

    async function search() {
        const keyword = input.value.trim();
        if (keyword === '') {
            close();
            return;
        }
        const mine = ++latest;
        let items;
        try {
            const response = await fetch(panel.dataset.endpoint + '?q=' + encodeURIComponent(keyword));
            if (!response.ok) {
                return;
            }
            items = await response.json();
        } catch (err) {
            // 通信に失敗したときは候補を出さない。下の一覧からは選べる
            return;
        }
        if (mine !== latest) {
            return;
        }
        panel.innerHTML = items.length > 0
            ? items.map(rowHtml).join('')
            : '<li class="suggest-empty">' + panel.dataset.noHit + '</li>';
        panel.hidden = false;
    }

    input.addEventListener('input', search);

    // 検索欄でEnterを押すと、フォームの先頭にある送信ボタン
    // （＝一覧の1行目）が押されたことになってしまうので止める
    input.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
        }
        if (event.key === 'Escape') {
            close();
        }
    });

    document.addEventListener('click', (event) => {
        if (event.target.closest('.search') === null) {
            close();
        }
    });
});
