// その欄で使えない字が打たれたとき、黙って消さずに理由を出す。
// 打てたのに後で怒られるより、打った時点で分かるほうが直しやすい。
// どの欄に何を許すかは data-allow で決め、知らせ先は data-warn-for で結ぶ。
// ここが動かなくてもサーバ側の検証は変わらず働く（打ててしまい、確認のときに弾かれるだけ）
document.addEventListener('DOMContentLoaded', () => {

    // 口座番号。全角で打たれた数字は半角に直し、それ以外の字は入れない
    const digitsOnly = (text) => {
        let converted = '';
        let dropped = '';
        for (const ch of window.bankTransferText.toHalfDigits(text)) {
            if (ch >= '0' && ch <= '9') {
                converted += ch;
            } else {
                dropped += ch;
            }
        }
        return { text: converted, dropped: dropped };
    };

    // 口座名義。打った字がその場で半角カタカナになる欄。
    //   ・IMEをオフにして "yamada" と打つ → 打った端から ﾔﾏﾀﾞ になる
    //   ・IMEをオンにして ヤマダ と打つ  → 確定した瞬間に ﾔﾏﾀﾞ になる
    //     （変換中に欄を書き換えるとIMEの入力そのものが壊れるので、そこは触らない）
    // 「ky」のような打ちかけのローマ字は、続きが来るまで英字のまま残す
    const halfWidthName = (text, resolve) => {
        const romaji = window.bankTransferText.fromRomaji(text, resolve);
        let converted = '';
        let dropped = romaji.dropped;
        for (const ch of romaji.text) {
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                converted += ch;
                continue;
            }
            const one = window.bankTransferText.toHalfWidthName(ch);
            converted += one.text;
            dropped += one.dropped;
        }
        return { text: converted, dropped: dropped };
    };

    const RULES = {
        digits: {
            convert: digitsOnly,
            message: '数字以外は入力できません'
        },
        kana: {
            convert: halfWidthName,
            message: 'カタカナ以外は入力できません'
        }
    };

    document.querySelectorAll('input[data-allow]').forEach((input) => {
        const rule = RULES[input.dataset.allow];
        if (rule === undefined) {
            return;
        }
        const warn = document.querySelector('[data-warn-for="' + input.id + '"]');
        const limit = Number(input.maxLength);

        // 知らせるのは「この欄に何が入るか」だけにする。
        // 打つたびに字を挙げて言い直すと、直し方より字のほうが目に入る
        const tell = (dropped, over) => {
            if (warn === null) {
                return;
            }
            let message = '';
            if (dropped !== '') {
                message = rule.message;
            } else if (over) {
                message = limit + '文字までです';
            }
            warn.textContent = message;
            warn.hidden = message === '';
        };

        // resolve は「もう続きは打たれない」ことの合図。
        // 欄から離れるときと送るときだけ真にして、打ちかけを片付ける
        const apply = (event, resolve) => {
            // 変換の途中（IMEで打っている最中）に書き換えると、打てなくなる。
            // 確定したあとに compositionend でもう一度通す
            if (event !== null && event.isComposing) {
                return;
            }
            const result = rule.convert(input.value, resolve);
            // 濁点は半角にすると2字になるので、直した結果が上限を超えることがある。
            // maxlength は打つときにしか効かず、JSで入れた値には効かないのでここで切る
            const over = limit > 0 && result.text.length > limit;
            const text = over ? result.text.slice(0, limit) : result.text;
            if (text !== input.value) {
                // 書き換えるとカーソルが末尾へ飛ぶので、
                // 「カーソルより前を同じ規則で直した長さ」を新しい位置にする
                const before = rule.convert(input.value.slice(0, input.selectionStart)).text.length;
                const caret = Math.min(before, text.length);
                input.value = text;
                input.setSelectionRange(caret, caret);
            }
            tell(result.dropped, over);
        };

        input.addEventListener('input', apply);
        input.addEventListener('compositionend', apply);
        input.addEventListener('blur', () => apply(null, true));
        if (input.form !== null) {
            input.form.addEventListener('submit', () => apply(null, true));
        }
    });
});
