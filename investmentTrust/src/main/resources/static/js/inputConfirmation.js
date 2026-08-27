/*
 * 投資信託注文情報入力画面のフロント側入力チェック・入力支援。
 *
 * サーバサイド（InvestmentTrustForm のアノテーション）と同じ条件をここでも判定し、
 * 送信前にその場でエラーを表示する。
 * JSは開発者ツールで無効化できるので、これは「入力しやすくするための仕組み」であり、
 * 最終的な可否の判断は必ずサーバサイドで行う。
 */

// サーバサイドの @Pattern / @Size / @Min / @Max と同じ条件をここでも定義する
const BANK_CODE_PATTERN = /^[0-9]{4}$/;     //  金融機関コードは半角数字4桁ちょうど
const BRANCH_CODE_PATTERN = /^[0-9]{3}$/;   //  支店コードは半角数字3桁ちょうど
const ACCOUNT_NUM_PATTERN = /^[0-9]{7}$/;   //  半角数字7桁ちょうど
const MONEY_PATTERN = /^[0-9,]+$/;          //  半角数字と、表示用のカンマだけ
const KANA_PATTERN = /^[ｦ-ﾟ ]+$/;  //  半角カタカナ(U+FF66 ｦ 〜 U+FF9F ﾟ)と半角スペースのみ
const NAME_MAX_LENGTH = 20;                 //  DBの name 列 varchar(20) に合わせる
const MONEY_MIN = 10000;
const MONEY_MAX = 10000000;

const form = document.getElementById("investmentTrustForm");
const nameInput = document.getElementById("name");
const moneyInput = document.getElementById("money");
const bankCodeInput = document.getElementById("bankCode");
const branchCodeInput = document.getElementById("branchCode");
const bankNameView = document.getElementById("bankName_view");
const branchNameView = document.getElementById("branchName_view");

/*
 * コード入力欄の「マスタに問い合わせた結果」は、入力欄の data-found 属性に持たせている。
 *   未設定 … まだ問い合わせていない
 *   "1"    … 見つかった
 *   "0"    … 見つからなかった
 *
 * 真偽値ではなく3状態にしているのは、「まだ問い合わせていない」を区別したいため。
 * 未問い合わせのまま送信された場合はフロントでは判定せず、サーバの判定に任せる
 * （フロントで勝手にエラーにすると、通信が遅いだけで送信できなくなってしまう）。
 */
const notFoundMessage = (id, message) =>
    (document.getElementById(id).dataset.found === "0") ? message : "";

/*
 * 入力欄ごとの判定ルール。
 * validate は入力値（前後の空白を除いたもの）を受け取り、
 * エラーメッセージを返す。問題が無ければ空文字を返す。
 */
const rules = [
    {
        id: "bankCode",
        validate: (value) => {
            if (value === "") {
                return "金融機関コードを入力してください。";
            }
            if (!BANK_CODE_PATTERN.test(value)) {
                return "金融機関コードは半角数字4桁で入力してください。";
            }
            //  マスタに問い合わせた結果、見つからなかったと分かっている場合だけエラーにする。
            //  まだ問い合わせていない場合は判定しない（サーバ側で必ず確認されるため）。
            return notFoundMessage("bankCode", "該当する金融機関がありません。");
        }
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
        id: "branchCode",
        validate: (value) => {
            if (value === "") {
                return "支店コードを入力してください。";
            }
            if (!BRANCH_CODE_PATTERN.test(value)) {
                return "支店コードは半角数字3桁で入力してください。";
            }
            return notFoundMessage("branchCode", "該当する支店がありません。");
        }
    },
    {
        id: "bankAccountType",
        radio: true,    //  ラジオボタンの項目。値の取り出し方などが他の欄と違うので目印を付けている
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

/* ============================================================================
 * ラジオボタン（科目名）は1つの項目が選択肢の数だけ <input> に分かれるため、
 * 「値の取り出し方」「赤枠を付ける場所」「イベントを登録する対象」が他の欄と異なる。
 * th:field を付けた影響で id も bankAccountType1, bankAccountType2 … と連番になるので、
 * radio: true が付いた項目だけ、以下のヘルパーで扱いを切り替えている。
 * ========================================================================== */

/** 項目に属する入力欄をすべて返す。ラジオボタンは選択肢の数だけ存在するのでnameで取得する */
const getInputs = (rule) => (rule.radio
    ? Array.from(document.getElementsByName(rule.id))
    : [document.getElementById(rule.id)]);

/** 判定に使う値を返す。ラジオボタンは選択中の値、未選択なら空文字 */
const getValue = (rule) => {
    if (!rule.radio) {
        return document.getElementById(rule.id).value.trim();
    }
    const checked = getInputs(rule).find((input) => input.checked);
    return (checked === undefined) ? "" : checked.value;
};

/** 赤枠を付ける要素。ラジオボタンは1つ1つではなく、選択肢全体を囲む要素に付ける */
const getErrorTarget = (rule) =>
    document.getElementById(rule.radio ? rule.id + "_group" : rule.id);

/** エラー時にカーソルを移す要素。ラジオボタンは選択中のもの、未選択なら先頭のボタン */
const getFocusTarget = (rule) => {
    const inputs = getInputs(rule);
    return inputs.find((input) => input.checked) || inputs[0];
};

/** 判定結果を画面に反映する。戻り値はエラーメッセージ（正常なら空文字） */
const showResult = (rule) => {
    const errorArea = document.getElementById(rule.id + "_error");
    const message = rule.validate(getValue(rule));

    errorArea.textContent = message;                                        //  サーバから返ってきたメッセージもここで上書きされる
    getErrorTarget(rule).classList.toggle("input-error", message !== "");   //  classList.toggle:第2引数がtrueなら付与、falseなら削除
    return message;
};

/** 項目IDを指定して判定し直す */
const showResultById = (id) => showResult(rules.find((rule) => rule.id === id));

/** エラー表示中の項目だけ、値が変わったタイミングで判定し直す */
const refreshIfShowing = (id) => {
    if (document.getElementById(id + "_error").textContent !== "") {
        showResultById(id);
    }
};

/** すべての入力欄のエラー表示を初期化する */
const clearAllErrors = () => {
    rules.forEach((rule) => {
        document.getElementById(rule.id + "_error").textContent = "";
        getErrorTarget(rule).classList.remove("input-error");
    });
};

/* ============================================================================
 * 金融機関コード・支店コード:入力されたコードでマスタを引き、名称を表示する
 *
 * ここで呼んでいるAPI（/api/banks/...）はあくまで入力支援。
 * 「APIが200を返したから正しい」とは考えないこと。JSは開発者ツールで無効化でき、
 * APIを一度も呼ばずに直接POSTすることもできる。
 * 実在するコードかどうかの最終判定は、サーバ側の
 * InvestmentTrustController#validateAndResolveMaster が行っている。
 * ========================================================================== */

/** 名称表示を消し、「まだ問い合わせていない」状態に戻す */
const clearMasterName = (input, view) => {
    delete input.dataset.found;
    view.textContent = "";
};

/** 問い合わせ結果を画面に反映する。見つからなかった場合は name に null が渡る */
const applyMasterResult = (input, view, id, name) => {
    input.dataset.found = (name === null) ? "0" : "1";
    view.textContent = (name === null) ? "" : name;
    showResultById(id);     //  「該当なし」をその場で表示する。直っていればエラーが消える
};

/** 404は「見つからなかった」としてnullを返す。それ以外の異常は例外にする */
const toJsonOrNull = (response) => {
    if (response.status === 404) {
        return null;
    }
    if (!response.ok) {
        throw new Error("APIの呼び出しに失敗しました: " + response.status);
    }
    return response.json();
};

/** 金融機関コードからマスタを引く */
const lookupBank = () => {
    const code = bankCodeInput.value.trim();
    if (!BANK_CODE_PATTERN.test(code)) {
        clearMasterName(bankCodeInput, bankNameView);   //  4桁になっていない間は問い合わせない
        return;
    }
    fetch("/api/banks/" + encodeURIComponent(code))
        .then(toJsonOrNull)
        .then((bank) => {
            applyMasterResult(bankCodeInput, bankNameView, "bankCode",
                (bank === null) ? null : bank.bankName);
            lookupBranch();     //  金融機関が変われば、同じ支店コードでも別の支店になる
        })
        //  通信できないときは「分からない」状態に戻し、判定はサーバに任せる
        .catch(() => clearMasterName(bankCodeInput, bankNameView));
};

/** 支店コードからマスタを引く。支店は金融機関とセットでないと特定できないので両方渡す */
const lookupBranch = () => {
    const bankCode = bankCodeInput.value.trim();
    const code = branchCodeInput.value.trim();
    if (!BANK_CODE_PATTERN.test(bankCode) || !BRANCH_CODE_PATTERN.test(code)) {
        clearMasterName(branchCodeInput, branchNameView);
        return;
    }
    fetch("/api/banks/" + encodeURIComponent(bankCode) + "/branches/" + encodeURIComponent(code))
        .then(toJsonOrNull)
        .then((branch) => applyMasterResult(branchCodeInput, branchNameView, "branchCode",
            (branch === null) ? null : branch.branchName))
        .catch(() => clearMasterName(branchCodeInput, branchNameView));
};

//  入力の途中で前に引いた名称が残っていると誤解を招くので、値が変わった時点で消す
bankCodeInput.addEventListener("input", () => clearMasterName(bankCodeInput, bankNameView));
branchCodeInput.addEventListener("input", () => clearMasterName(branchCodeInput, branchNameView));

//  入力を終えて別の項目へ移ったとき（change）と、「検索」ボタンを押したときに問い合わせる
bankCodeInput.addEventListener("change", lookupBank);
branchCodeInput.addEventListener("change", lookupBranch);
document.getElementById("bankCodeSearch").addEventListener("click", lookupBank);
document.getElementById("branchCodeSearch").addEventListener("click", lookupBranch);

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
    let firstErrorRule = null;

    rules.forEach((rule) => {
        if (showResult(rule) !== "" && firstErrorRule === null) {
            firstErrorRule = rule;
        }
    });

    if (firstErrorRule !== null) {
        e.preventDefault();                             //  送信を中止する
        getFocusTarget(firstErrorRule).focus();         //  最初にエラーになった項目へカーソルを移す
        return;
    }
    moneyInput.value = toDigits(moneyInput.value);          //  サーバへは表示用のカンマを外した数字だけを送る
});

rules.forEach((rule) => {
    const errorArea = document.getElementById(rule.id + "_error");

    // ラジオボタンは選択肢の数だけ <input> があるので、そのすべてに登録する
    getInputs(rule).forEach((input) => {
        // 入力途中で誤ったメッセージを出さないよう、
        // すでにエラーが出ている項目だけ入力のたびに再判定する（直したらすぐ消える）
        input.addEventListener("input", () => {
            if (errorArea.textContent !== "") {
                showResult(rule);
            }
        });

        // 入力を終えて別の項目へ移ったタイミング、およびプルダウンやラジオボタンを選び直したタイミングで判定する
        input.addEventListener("change", () => showResult(rule));
    });
});

// 「クリア」ボタンで値を戻したときにエラー表示も消す。
// reset処理が終わったあとに実行したいので setTimeout で後ろにずらしている
form.addEventListener("reset", () => window.setTimeout(() => {
    clearAllErrors();
    clearMasterName(bankCodeInput, bankNameView);
    clearMasterName(branchCodeInput, branchNameView);
    formatMoney();
}, 0));

// 入力エラーでサーバから戻ってきたときなど、最初から値が入っている場合もカンマを付ける
formatMoney();
