// 金額入力欄を3桁ごとのカンマ区切りで表示する
// サーバへは数字だけを送りたいので、送信の直前にカンマを取り除く
document.addEventListener('DOMContentLoaded', () => {
    const moneyInput = document.getElementById('money');
    if (moneyInput === null) {
        return;
    }

    // 全角で打たれた数字は半角に直してから数える。口座番号の欄と同じ規則なので、
    // 変換そのものは bankTransferText.js が持っている
    const toHalfDigits = window.bankTransferText.toHalfDigits;

    // 上限を超える桁はそもそも打てないようにする。上限が2,000,000なら8桁目から先は入らない。
    // 打ててから確認画面で怒られるより、入らないほうが早い。
    // ただし桁で止めても上限は超えられる（9,999,999は7桁）ので、値でも見て知らせる。
    // 上限そのものはサーバが持っていて、data-max で受け取る
    const maxTransfer = Number(moneyInput.dataset.max) || 0;
    const maxDigits = maxTransfer > 0 ? String(maxTransfer).length : 0;

    // 数字にもカンマにもならない字は入れない。何が入らなかったのかを知らせる
    const warn = document.querySelector('[data-warn-for="money"]');
    const tell = (dropped, over) => {
        if (warn === null) {
            return;
        }
        let message = '';
        if (over) {
            message = '1回の振込は ' + maxTransfer.toLocaleString('ja-JP') + ' 円までです';
        } else if (dropped !== '') {
            message = '数字以外は入力できません';
        }
        warn.textContent = message;
        warn.hidden = message === '';
    };

    // 数字以外を取り除いたうえで3桁ごとにカンマを入れる
    // Numberを経由すると桁数が大きいときに値が変わってしまうため、文字列のまま組み立てる
    const format = (value) => {
        let digits = toHalfDigits(value).replace(/[^0-9]/g, '');
        if (maxDigits > 0) {
            digits = digits.slice(0, maxDigits);
        }
        let formatted = '';
        for (let i = 0; i < digits.length; i++) {
            if (i > 0 && (digits.length - i) % 3 === 0) {
                formatted += ',';
            }
            formatted += digits[i];
        }
        return formatted;
    };

    // 整形し直すとカーソルが末尾へ飛ぶので、
    // 「カーソルより前にある数字の個数」を目印にして元の位置へ戻す
    const moveCaret = (digitCount) => {
        if (digitCount === 0) {
            moneyInput.setSelectionRange(0, 0);
            return;
        }
        let counted = 0;
        //カンマを抜いた文字数をcountedに格納する
        for (let i = 0; i < moneyInput.value.length; i++) {
            if (moneyInput.value[i] !== ',') {
                counted++;
            }
            //カーソルより前の数字の数とcountedがそろったら一文字みぎにずらして終了
            if (counted === digitCount) {
                moneyInput.setSelectionRange(i + 1, i + 1);
                return;
            }
        }
    };

    moneyInput.addEventListener('input', () => {
        const before = toHalfDigits(moneyInput.value);
        const digitsBeforeCaret = toHalfDigits(moneyInput.value.slice(0, moneyInput.selectionStart)).replace(/[^0-9]/g, '').length;//カーソルより前を切り出す
        moneyInput.value = format(moneyInput.value);//コンマを追加
        moveCaret(digitsBeforeCaret);//カーソルをずらす
        // 入らなかった字と、入ったけれど上限を超えている額を知らせる
        const shown = Number(moneyInput.value.replace(/,/g, '') || 0);
        tell(before.replace(/[0-9,]/g, ''), maxTransfer > 0 && shown > maxTransfer);
    });

    // 入力エラーで入力画面に戻ってきたときのために、表示時にも整形しておく
    moneyInput.value = format(moneyInput.value);

    // カンマが付いたまま送るとサーバ側の数値変換に失敗するため、送信直前に外す
    moneyInput.form.addEventListener('submit', () => {
        moneyInput.value = moneyInput.value.replace(/,/g, '');
    });
});

// よく使う日付の近道。カレンダーを開かずに指定できるようにする
document.addEventListener('DOMContentLoaded', () => {
    const dateInput = document.getElementById('transferDateTime');
    if (dateInput === null) {
        return;
    }

    // 欄のどこを押してもカレンダーが開くようにする（小さいアイコンを狙わせない）
    dateInput.addEventListener('click', () => {
        if (typeof dateInput.showPicker === 'function') {
            try {
                dateInput.showPicker();
            } catch (err) {
                // 対応していない場合はブラウザ既定の動作に任せる
            }
        }
    });

    document.querySelectorAll('.chip[data-days]').forEach((chip) => {
        chip.addEventListener('click', () => {
            const date = new Date();
            date.setDate(date.getDate() + Number(chip.dataset.days));
            const pad = (n) => String(n).padStart(2, '0');
            dateInput.value = date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate());
        });
    });
});


// 相手口座に届く額を打ちながら見せる。手数料を含めるかどうかで変わる。
// 実際に決めるのはサーバ側で、ここは確認画面まで進まないと分からない状態を減らすだけ
document.addEventListener('DOMContentLoaded', () => {
    const moneyInput = document.getElementById('money');
    const result = document.getElementById('feeResult');
    const includeInput = document.querySelector('.switch input[type="checkbox"]');
    if (moneyInput === null || result === null || includeInput === null) {
        return;
    }
    const feeUnder = Number(result.dataset.feeUnder);
    const feeOver = Number(result.dataset.feeOver);
    const threshold = Number(result.dataset.threshold);

    const update = () => {
        const entered = Number(moneyInput.value.replace(/[^0-9]/g, ''));
        if (entered === 0) {
            result.textContent = '';
            return;
        }
        // 手数料の段はサーバと同じく「打った額」で決める
        const fee = entered < threshold ? feeUnder : feeOver;
        const toPayee = includeInput.checked ? entered - fee : entered;
        result.textContent = '相手口座への振込額 '
                + Math.max(0, toPayee).toLocaleString('ja-JP') + '円';
    };

    moneyInput.addEventListener('input', update);
    includeInput.addEventListener('change', update);
    update();
});
