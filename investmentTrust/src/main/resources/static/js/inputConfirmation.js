/*
 * 投資信託注文情報入力画面のフロント側入力チェック・入力支援。
 *
 * サーバサイド（InvestmentTrustForm のアノテーション）と同じ条件をここでも判定し、
 * 送信前にその場でエラーを表示する。
 * JSは開発者ツールで無効化できるので、これは「入力しやすくするための仕組み」であり、
 * 最終的な可否の判断は必ずサーバサイドで行う。
 */

// サーバサイドの @Pattern / @Size / @Min / @Max と同じ条件をここでも定義する
const ACCOUNT_NUM_PATTERN = /^[0-9]{7}$/;   //  半角数字7桁ちょうど
const MONEY_PATTERN = /^[0-9,]+$/;          //  半角数字と、表示用のカンマだけ
const KANA_PATTERN = /^[ｦ-ﾟ ]+$/;  //  半角カタカナ(U+FF66 ｦ 〜 U+FF9F ﾟ)と半角スペースのみ
const NAME_MAX_LENGTH = 20;                 //  DBの name 列 varchar(20) に合わせる
const MONEY_MIN = 10000;
const MONEY_MAX = 10000000;

const form = document.getElementById("investmentTrustForm");
const nameInput = document.getElementById("name");
const moneyInput = document.getElementById("money");

/*
 * 入力欄ごとの判定ルール。
 * validate は入力値（前後の空白を除いたもの）を受け取り、
 * エラーメッセージを返す。問題が無ければ空文字を返す。
 */
const rules = [
    {
        id: "bankName",
        validate: (value) => (value === "" ? "金融機関名を選択してください。" : "")
    },
    {
        id: "bankAccountNum",
        validate: (value) => {
            if (value === "") {
                return "口座番号を入力してください。";
            }
            if (!ACCOUNT_NUM_PATTERN.test(value)) {
                return "口座番号は半角数字7桁で入力してください。";
            }
            return "";
        }
    },
    {
        id: "branchName",
        validate: (value) => (value === "" ? "支店名を選択してください。" : "")
    },
    {
        id: "bankAccountType",
        validate: (value) => (value === "" ? "科目名を選択してください。" : "")
    },
    {
        id: "name",
        validate: (value) => {
            if (value === "") {
                return "購入者名を入力してください。";
            }
            if (!KANA_PATTERN.test(value)) {
                return "購入者名は半角カナ（半角スペース可）で入力してください。";
            }
            if (value.length > NAME_MAX_LENGTH) {
                return "購入者名は" + NAME_MAX_LENGTH + "文字以内で入力してください。";
            }
            return "";
        }
    },
    {
        id: "fundName",
        validate: (value) => (value === "" ? "銘柄を選択してください。" : "")
    },
    {
        id: "money",
        validate: (value) => {
            if (value === "") {
                return "金額を入力してください。";
            }
            if (!MONEY_PATTERN.test(value)) {
                return "金額は半角数字（1円単位）で入力してください。";
            }
            const money = Number(value.replace(/,/g, ""));   //  表示用のカンマを外してから比較する
            if (money < MONEY_MIN) {
                return "金額は" + MONEY_MIN.toLocaleString() + "円以上で入力してください。";
            }
            if (money > MONEY_MAX) {
                return "金額は" + MONEY_MAX.toLocaleString() + "円以下で入力してください。";
            }
            return "";
        }
    }
];

/** 判定結果を画面に反映する。戻り値はエラーメッセージ（正常なら空文字） */
const showResult = (rule) => {
    const input = document.getElementById(rule.id);
    const errorArea = document.getElementById(rule.id + "_error");
    const message = rule.validate(input.value.trim());

    errorArea.textContent = message;                            //  サーバから返ってきたメッセージもここで上書きされる
    input.classList.toggle("input-error", message !== "");      //  classList.toggle:第2引数がtrueなら付与、falseなら削除
    return message;
};

/** エラー表示中の項目だけ、値が変わったタイミングで判定し直す */
const refreshIfShowing = (id) => {
    if (document.getElementById(id + "_error").textContent !== "") {
        showResult(rules.find((rule) => rule.id === id));
    }
};

/** すべての入力欄のエラー表示を初期化する */
const clearAllErrors = () => {
    rules.forEach((rule) => {
        document.getElementById(rule.id + "_error").textContent = "";
        document.getElementById(rule.id).classList.remove("input-error");
    });
};

/* ============================================================================
 * 金額欄:3桁ごとにカンマを入れて見やすくする
 *
 * カンマ付きのままサーバへ送るとInteger型に変換できずエラーになるため、
 * 画面上だけカンマを付け、送信の直前に外している（form の submit を参照）。
 * ========================================================================== */

/** 数字以外（カンマなど）を取り除く。先頭の余分な0も落とす（0010000 -> 10000） */
const toDigits = (value) => value.replace(/[^0-9]/g, "").replace(/^0+(?=[0-9])/, "");

/** 1234567 -> 1,234,567 。「右から3桁ずつの区切り目」にカンマを差し込む */
const withComma = (digits) => digits.replace(/\B(?=([0-9]{3})+$)/g, ",");

/**
 * 金額欄の表示をカンマ付きに整える。
 * カンマを差し込むと文字数が変わりカーソル位置がずれるので、
 * 「カーソルより前にある数字の個数」を数えておき、
 * 整形後に同じ個数だけ数え直した位置へカーソルを戻している。
 */
const formatMoney = () => {
    const before = moneyInput.value;
    const formatted = withComma(toDigits(before));
    if (formatted === before) {
        return;
    }
    const caret = moneyInput.selectionStart;
    const digitCount = (caret === null) ? -1 : before.slice(0, caret).replace(/[^0-9]/g, "").length;

    moneyInput.value = formatted;

    if (digitCount >= 0 && document.activeElement === moneyInput) {
        let position = 0;
        let counted = 0;
        while (position < formatted.length && counted < digitCount) {
            if (formatted[position] !== ",") {
                counted++;
            }
            position++;
        }
        moneyInput.setSelectionRange(position, position);
    }
    refreshIfShowing("money");
};

/* ============================================================================
 * 購入者名欄:入力された文字を強制的に半角へ変換する
 *
 * 全角カナ・ひらがな・全角英数字を半角に直してから判定するので、
 * 「ヤマダ タロウ」「やまだ たろう」と入力しても「ﾔﾏﾀﾞ ﾀﾛｳ」として扱われる。
 * ただし漢字（山田）には対応する半角文字が存在しないため変換できず、エラーになる。
 * ========================================================================== */

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

/** 購入者名欄の内容を半角に置き換える。カーソル位置は変換後の文字数に合わせて戻す */
const convertNameToHankaku = () => {
    const before = nameInput.value;
    const after = toHankaku(before);
    if (after === before) {
        return;
    }
    const caret = nameInput.selectionStart;
    const newCaret = (caret === null) ? after.length : toHankaku(before.slice(0, caret)).length;

    nameInput.value = after;

    if (document.activeElement === nameInput) {
        nameInput.setSelectionRange(newCaret, newCaret);
    }
    refreshIfShowing("name");
};

//  日本語入力の変換中（未確定の状態）に値を書き換えると入力が壊れるので、
//  compositionstart 〜 compositionend の間は変換しない
let composing = false;
nameInput.addEventListener("compositionstart", () => {
    composing = true;
});
nameInput.addEventListener("compositionend", () => {
    composing = false;
    convertNameToHankaku();
});
nameInput.addEventListener("input", () => {
    if (!composing) {
        convertNameToHankaku();
    }
});
nameInput.addEventListener("blur", convertNameToHankaku);

moneyInput.addEventListener("input", formatMoney);

// 送信時にすべての項目を判定する。
// 「確認」ボタンのclickではなくformのsubmitを見ることで、
// 入力欄でEnterキーを押して送信された場合もチェックが効くようにしている。
form.addEventListener("submit", (e) => {
    convertNameToHankaku();     //  変換されないまま送信されるのを防ぐ
    let firstErrorId = null;

    rules.forEach((rule) => {
        if (showResult(rule) !== "" && firstErrorId === null) {
            firstErrorId = rule.id;
        }
    });

    if (firstErrorId !== null) {
        e.preventDefault();                                 //  送信を中止する
        document.getElementById(firstErrorId).focus();      //  最初にエラーになった項目へカーソルを移す
        return;
    }
    moneyInput.value = toDigits(moneyInput.value);          //  サーバへは表示用のカンマを外した数字だけを送る
});

rules.forEach((rule) => {
    const input = document.getElementById(rule.id);
    const errorArea = document.getElementById(rule.id + "_error");

    // 入力途中で誤ったメッセージを出さないよう、
    // すでにエラーが出ている項目だけ入力のたびに再判定する（直したらすぐ消える）
    input.addEventListener("input", () => {
        if (errorArea.textContent !== "") {
            showResult(rule);
        }
    });

    // 入力を終えて別の項目へ移ったタイミング、およびプルダウンを選び直したタイミングで判定する
    input.addEventListener("change", () => showResult(rule));
});

// 「クリア」ボタンで値を戻したときにエラー表示も消す。
// reset処理が終わったあとに実行したいので setTimeout で後ろにずらしている
form.addEventListener("reset", () => window.setTimeout(() => {
    clearAllErrors();
    formatMoney();
}, 0));

// 入力エラーでサーバから戻ってきたときなど、最初から値が入っている場合もカンマを付ける
formatMoney();
