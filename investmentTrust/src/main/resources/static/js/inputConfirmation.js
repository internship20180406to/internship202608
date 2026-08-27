/*
 * 投資信託注文情報入力画面のフロント側入力チェック・入力支援。
 *
 * サーバサイド（InvestmentTrustForm のアノテーション）と同じ条件をここでも判定し、
 * 送信前にその場でエラーを表示する。
 * JSは開発者ツールで無効化できるので、これは「入力しやすくするための仕組み」であり、
 * 最終的な可否の判断は必ずサーバサイドで行う。
 */

// サーバサイドの @Pattern / @Size / @Min / @Max と同じ条件をここでも定義する
//  金融機関コード・支店コードの書式（BANK_CODE_PATTERN / BRANCH_CODE_PATTERN）は
//  bankBranchPicker.js が定義している。あちらを先に読み込むこと。
const ACCOUNT_NUM_PATTERN = /^[0-9]{7}$/;   //  半角数字7桁ちょうど
//  MONEY_PATTERN は numberFormat.js、KANA_PATTERN は hankakuKana.js が定義している。
//  どちらもこのファイルより先に読み込むこと。
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
            return bankBranchNotFound("bankCode") ? "該当する金融機関がありません。" : "";
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
            return bankBranchNotFound("branchCode") ? "該当する支店がありません。" : "";
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
 * 金融機関コード・支店コードの入力部品
 *
 * 「コードを打つと候補が並び、選んで『選択』ボタンで確定する」という動きは
 * bankBranchPicker.js にまとめてあり、口座登録画面と共通で使っている。
 * ここから渡しているのは「値が変わったら、その項目の入力チェックを掛け直す」という
 * この画面側の都合だけ。
 * ========================================================================== */
setupBankBranchPickers({
    //  「選択」で確定したときは結果をそのまま表示する
    confirmed: showResultById,
    //  入力中や候補をクリックしただけのときは、すでにエラーが出ている項目だけ掛け直す。
    //  こうしないとコードを1文字打った時点で「4桁で入力してください」と出てしまう
    editing: refreshIfShowing
});

/* ============================================================================
 * 金額欄と購入者名欄の入力支援
 *
 * 3桁ごとのカンマ区切り（numberFormat.js）と、半角カナへの自動変換（hankakuKana.js）は
 * 口座登録画面でも同じものを使うので、別ファイルに切り出してある。
 * ここでは「対象の入力欄」と「変換したあとに何をするか」だけを渡す。
 * ========================================================================== */

//  戻り値は、送信直前など手動で整形・変換したいときに呼ぶための関数
const formatMoney = setupCommaInput(moneyInput, () => refreshIfShowing("money"));
const convertNameToHankaku = setupHankakuInput(nameInput, () => refreshIfShowing("name"));

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
    resetBankBranchPickers();
    formatMoney();
}, 0));

//  入力エラーでサーバから戻ってきたときなど、最初から値が入っている場合の整形は
//  setupCommaInput の中で済ませている
