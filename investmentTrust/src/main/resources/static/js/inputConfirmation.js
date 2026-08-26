/*
 * 投資信託注文情報入力画面のフロント側入力チェック。
 *
 * サーバサイド（InvestmentTrustForm のアノテーション）と同じ条件をここでも判定し、
 * 送信前にその場でエラーを表示する。
 * JSは開発者ツールで無効化できるので、これは「入力しやすくするための仕組み」であり、
 * 最終的な可否の判断は必ずサーバサイドで行う。
 */

// サーバサイドの @Pattern / @Size / @Min / @Max と同じ条件をここでも定義する
const ACCOUNT_NUM_PATTERN = /^[0-9]{7}$/;   //  半角数字7桁ちょうど
const MONEY_PATTERN = /^[0-9]+$/;           //  小数・符号なしの半角数字（1円単位）
const KANA_PATTERN = /^[\uFF66-\uFF9F ]+$/;  //  半角カタカナ(U+FF66 ｦ 〜 U+FF9F ﾟ)と半角スペースのみ
const NAME_MAX_LENGTH = 20;                 //  DBの name 列 varchar(20) に合わせる
const MONEY_MIN = 10000;
const MONEY_MAX = 10000000;

const form = document.getElementById("investmentTrustForm");

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
            const money = Number(value);
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

/** すべての入力欄を消してエラー表示も初期化する */
const clearAllErrors = () => {
    rules.forEach((rule) => {
        document.getElementById(rule.id + "_error").textContent = "";
        document.getElementById(rule.id).classList.remove("input-error");
    });
};

// 送信時にすべての項目を判定する。
// 「確認」ボタンのclickではなくformのsubmitを見ることで、
// 入力欄でEnterキーを押して送信された場合もチェックが効くようにしている。
form.addEventListener("submit", (e) => {
    let firstErrorId = null;

    rules.forEach((rule) => {
        if (showResult(rule) !== "" && firstErrorId === null) {
            firstErrorId = rule.id;
        }
    });

    if (firstErrorId !== null) {
        e.preventDefault();                                 //  送信を中止する
        document.getElementById(firstErrorId).focus();       //  最初にエラーになった項目へカーソルを移す
    }
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
form.addEventListener("reset", () => window.setTimeout(clearAllErrors, 0));
