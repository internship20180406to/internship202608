// 検索欄に打つたびにサーバへ問い合わせ、入力欄の下に候補を重ねて出す。
// 下の一覧は差し替えないので、一覧は固定されたまま隠れるだけで済む。
document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('searchKeyword');
    const panel = document.getElementById('searchSuggest');
    if (input === null || panel === null) {
        return;
    }

    // 候補の1行。押すと、下の一覧と同じくフォームが送信されて選択が確定する。
    // 文字列をつないで innerHTML に入れるのではなく、要素を作って textContent に入れる。
    // 金融機関名・支店名はマスタから来る自由な文字列で、引用符や「<」が混ざると
    // 文字列の組み立てでは属性や要素の外へはみ出してしまう
    const row = (item) => {
        const button = document.createElement('button');
        button.className = 'picklist-row';
        button.type = 'submit';
        button.name = panel.dataset.paramName;
        button.value = item.code;

        const code = document.createElement('span');
        code.className = 'picklist-code';
        code.textContent = item.code;

        const name = document.createElement('span');
        name.textContent = item.name;

        const chevron = document.createElement('span');
        chevron.className = 'picklist-chevron';
        chevron.setAttribute('aria-hidden', 'true');
        chevron.textContent = String.fromCharCode(8250);

        button.append(code, name, chevron);

        const li = document.createElement('li');
        li.append(button);
        return li;
    };

    // 1件も見つからなかったときの1行
    const noHitRow = () => {
        const li = document.createElement('li');
        li.className = 'suggest-empty';
        li.textContent = panel.dataset.noHit;
        return li;
    };

    const close = () => {
        panel.hidden = true;
        panel.replaceChildren();
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
        panel.replaceChildren(...(items.length > 0 ? items.map(row) : [noHitRow()]));
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
