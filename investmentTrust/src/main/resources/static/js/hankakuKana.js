/*
 * 入力された文字を半角カナへ変換する部品（共通）。
 *
 * 全角カナ・ひらがな・全角英数字を半角に直してから判定するので、
 * 「ヤマダ タロウ」「やまだ たろう」と入力しても「ﾔﾏﾀﾞ ﾀﾛｳ」として扱われる。
 * ただし漢字（山田）には対応する半角文字が存在しないため変換できず、エラーになる。
 *
 * 投資信託の申込画面（購入者名）と口座登録画面（口座名義）の両方から使う。
 */

//  全角カタカナ -> 半角カタカナ の対応表。
//  濁点・半濁点付きの文字は半角では2文字に分かれる（ガ -> ｶ + ﾞ）ため、表で持つ必要がある。
const KANA_MAP = {
    "ガ": "ｶﾞ", "ギ": "ｷﾞ", "グ": "ｸﾞ", "ゲ": "ｹﾞ", "ゴ": "ｺﾞ",
    "ザ": "ｻﾞ", "ジ": "ｼﾞ", "ズ": "ｽﾞ", "ゼ": "ｾﾞ", "ゾ": "ｿﾞ",
    "ダ": "ﾀﾞ", "ヂ": "ﾁﾞ", "ヅ": "ﾂﾞ", "デ": "ﾃﾞ", "ド": "ﾄﾞ",
    "バ": "ﾊﾞ", "ビ": "ﾋﾞ", "ブ": "ﾌﾞ", "ベ": "ﾍﾞ", "ボ": "ﾎﾞ",
    "パ": "ﾊﾟ", "ピ": "ﾋﾟ", "プ": "ﾌﾟ", "ペ": "ﾍﾟ", "ポ": "ﾎﾟ",
    "ヴ": "ｳﾞ",
    "ア": "ｱ", "イ": "ｲ", "ウ": "ｳ", "エ": "ｴ", "オ": "ｵ",
    "カ": "ｶ", "キ": "ｷ", "ク": "ｸ", "ケ": "ｹ", "コ": "ｺ",
    "サ": "ｻ", "シ": "ｼ", "ス": "ｽ", "セ": "ｾ", "ソ": "ｿ",
    "タ": "ﾀ", "チ": "ﾁ", "ツ": "ﾂ", "テ": "ﾃ", "ト": "ﾄ",
    "ナ": "ﾅ", "ニ": "ﾆ", "ヌ": "ﾇ", "ネ": "ﾈ", "ノ": "ﾉ",
    "ハ": "ﾊ", "ヒ": "ﾋ", "フ": "ﾌ", "ヘ": "ﾍ", "ホ": "ﾎ",
    "マ": "ﾏ", "ミ": "ﾐ", "ム": "ﾑ", "メ": "ﾒ", "モ": "ﾓ",
    "ヤ": "ﾔ", "ユ": "ﾕ", "ヨ": "ﾖ",
    "ラ": "ﾗ", "リ": "ﾘ", "ル": "ﾙ", "レ": "ﾚ", "ロ": "ﾛ",
    "ワ": "ﾜ", "ヲ": "ｦ", "ン": "ﾝ",
    "ァ": "ｧ", "ィ": "ｨ", "ゥ": "ｩ", "ェ": "ｪ", "ォ": "ｫ",
    "ッ": "ｯ", "ャ": "ｬ", "ュ": "ｭ", "ョ": "ｮ",
    "ー": "ｰ", "・": "･", "、": "､", "。": "｡", "「": "｢", "」": "｣",
    "゛": "ﾞ", "゜": "ﾟ",
    "　": " "
};

/** 半角カタカナ(U+FF66 ｦ 〜 U+FF9F ﾟ)と半角スペースのみ */
const KANA_PATTERN = /^[ｦ-ﾟ ]+$/;

/** ひらがな -> カタカナ。文字コードがちょうど0x60ずれているので計算で変換できる */
const hiraganaToKatakana = (value) =>
    value.replace(/[ぁ-ゖ]/g, (char) => String.fromCharCode(char.charCodeAt(0) + 0x60));

/** 全角の英数字・記号 -> 半角。U+FF01〜U+FF5E は半角のASCIIとちょうど0xFEE0ずれている */
const zenkakuAsciiToHankaku = (value) =>
    value.replace(/[！-～]/g, (char) => String.fromCharCode(char.charCodeAt(0) - 0xFEE0));

/** 入力された文字をできる限り半角へ変換する */
const toHankaku = (value) => {
    const converted = zenkakuAsciiToHankaku(hiraganaToKatakana(value));
    let result = "";
    for (const char of converted) {
        result += (KANA_MAP[char] !== undefined) ? KANA_MAP[char] : char;
    }
    return result;
};

/**
 * 入力欄の内容を半角に置き換える。カーソル位置は変換後の文字数に合わせて戻す。
 * @param input     対象の入力欄
 * @param onChanged 変換したときに呼ばれる関数（入力チェックを掛け直すために使う）
 */
const convertInputToHankaku = (input, onChanged) => {
    const before = input.value;
    const after = toHankaku(before);
    if (after === before) {
        return;
    }
    const caret = input.selectionStart;
    const newCaret = (caret === null) ? after.length : toHankaku(before.slice(0, caret)).length;

    input.value = after;

    if (document.activeElement === input) {
        input.setSelectionRange(newCaret, newCaret);
    }
    onChanged();
};

/**
 * 入力欄に半角変換を仕掛ける。
 *
 * 日本語入力の変換中（未確定の状態）に値を書き換えると入力が壊れるので、
 * compositionstart 〜 compositionend の間は変換しない。
 *
 * @return 送信直前などに手動で変換したいときに呼ぶ関数
 */
const setupHankakuInput = (input, onChanged) => {
    const convert = () => convertInputToHankaku(input, onChanged);
    let composing = false;

    input.addEventListener("compositionstart", () => {
        composing = true;
    });
    input.addEventListener("compositionend", () => {
        composing = false;
        convert();
    });
    input.addEventListener("input", () => {
        if (!composing) {
            convert();
        }
    });
    input.addEventListener("blur", convert);

    return convert;
};
