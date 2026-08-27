/*
 * 金額欄を3桁ごとのカンマ区切りで表示する部品（共通）。
 *
 * カンマ付きのままサーバへ送ると数値型に変換できずエラーになるため、
 * 画面上だけカンマを付け、送信の直前に外す。
 * 外す処理は各画面のsubmit時に toDigits() を呼んで行う。
 *
 * 投資信託の申込画面（購入金額）と口座登録画面（初期残高）の両方から使う。
 */

/** 半角数字と、表示用のカンマだけ */
const MONEY_PATTERN = /^[0-9,]+$/;

/** 数字以外（カンマなど）を取り除く。先頭の余分な0も落とす（0010000 -> 10000） */
const toDigits = (value) => value.replace(/[^0-9]/g, "").replace(/^0+(?=[0-9])/, "");

/** 1234567 -> 1,234,567 。「右から3桁ずつの区切り目」にカンマを差し込む */
const withComma = (digits) => digits.replace(/\B(?=([0-9]{3})+$)/g, ",");

/**
 * 入力欄の表示をカンマ付きに整える。
 *
 * カンマを差し込むと文字数が変わりカーソル位置がずれるので、
 * 「カーソルより前にある数字の個数」を数えておき、
 * 整形後に同じ個数だけ数え直した位置へカーソルを戻している。
 *
 * @param input     対象の入力欄
 * @param onChanged 整形したときに呼ばれる関数（入力チェックを掛け直すために使う）
 */
const formatWithComma = (input, onChanged) => {
    const before = input.value;
    const formatted = withComma(toDigits(before));
    if (formatted === before) {
        return;
    }
    const caret = input.selectionStart;
    const digitCount = (caret === null) ? -1 : before.slice(0, caret).replace(/[^0-9]/g, "").length;

    input.value = formatted;

    if (digitCount >= 0 && document.activeElement === input) {
        let position = 0;
        let counted = 0;
        while (position < formatted.length && counted < digitCount) {
            if (formatted[position] !== ",") {
                counted++;
            }
            position++;
        }
        input.setSelectionRange(position, position);
    }
    onChanged();
};

/**
 * 入力欄にカンマ整形を仕掛ける。
 * 入力エラーでサーバから戻ってきたときなど、最初から値が入っている場合にも備えて
 * 仕掛けた直後に一度整形しておく。
 *
 * @return 手動で整形したいときに呼ぶ関数
 */
const setupCommaInput = (input, onChanged) => {
    const format = () => formatWithComma(input, onChanged);
    input.addEventListener("input", format);
    format();
    return format;
};
