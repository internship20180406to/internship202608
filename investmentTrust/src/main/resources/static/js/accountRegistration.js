/*
 * 口座登録画面のフロント側入力チェック・入力支援。
 *
 * サーバサイド（AccountRegistrationForm のアノテーション）と同じ条件をここでも判定し、
 * 送信前にその場でエラーを表示する。
 * JSは開発者ツールで無効化できるので、これは「入力しやすくするための仕組み」であり、
 * 最終的な可否の判断は必ずサーバサイドで行う。
 *
 * ★共通部品を先に読み込んでおくこと（accountRegistrationMain.html を参照）
 *     bankBranchPicker.js … 金融機関・支店のコード入力と候補一覧
 *     numberFormat.js     … 3桁ごとのカンマ区切り
 *     hankakuKana.js      … 半角カナへの自動変換
 */

const ACCOUNT_NUM_PATTERN = /^[0-9]{7}$/;   //  半角数字7桁ちょうど
const ACCOUNT_NAME_MAX_LENGTH = 20;         //  DBの accountName 列 varchar(20) に合わせる
const BALANCE_MIN = 0;
const BALANCE_MAX = 10000000000;

const form = document.getElementById("accountRegistrationForm");
const accountNameInput = document.getElementById("accountName");
const balanceInput = document.getElementById("balance");

/*
 * 入力欄ごとの判定ルール。
 * validate は入力値を受け取り、エラーメッセージを返す。問題が無ければ空文字を返す。
 * 並び順は画面の並びと合わせてある（最初にエラーになった項目へカーソルを移すため）。
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
        id: "accountType",
        radio: true,    //  ラジオボタンの項目。値の取り出し方などが他の欄と違うので目印を付けている
        validate: (value) => (value === "" ? "科目名を選択してください。" : "")
    },
    {
        id: "accountNum",
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
        id: "accountName",
        validate: (value) => {
            if (value === "") {
                return "口座名義を入力してください。";
            }
            if (!KANA_PATTERN.test(value)) {
                return "口座名義は半角カナ（半角スペース可）で入力してください。";
            }
            if (value.length > ACCOUNT_NAME_MAX_LENGTH) {
                return "口座名義は" + ACCOUNT_NAME_MAX_LENGTH + "文字以内で入力してください。";
            }
            return "";
        }
    },
    {
        id: "balance",
        validate: (value) => {
            if (value === "") {
                return "初期残高を入力してください。";
            }
            if (!MONEY_PATTERN.test(value)) {
                return "初期残高は半角数字（1円単位）で入力してください。";
            }
            const balance = Number(value.replace(/,/g, ""));    //  表示用のカンマを外してから比較する
            if (balance < BALANCE_MIN) {
                return "初期残高は" + BALANCE_MIN.toLocaleString() + "円以上で入力してください。";
            }
            if (balance > BALANCE_MAX) {
                return "初期残高は" + BALANCE_MAX.toLocaleString() + "円以下で入力してください。";
            }
            return "";
        }
    }
];

/* ============================================================================
 * 判定結果の表示
 *
 * 申込画面（inputConfirmation.js）と同じ作り。
 * ラジオボタンは1つの項目が選択肢の数だけ input に分かれるため、
 * 「値の取り出し方」「赤枠を付ける場所」「イベントを登録する対象」が他の欄と異なる。
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
 * 共通部品の組み込み
 * ========================================================================== */

setupBankBranchPickers({
    //  「選択」で確定したときは結果をそのまま表示する
    confirmed: showResultById,
    //  入力中や候補をクリックしただけのときは、すでにエラーが出ている項目だけ掛け直す。
    //  こうしないとコードを1文字打った時点で「4桁で入力してください」と出てしまう
    editing: refreshIfShowing
});

//  戻り値は、送信直前など手動で整形・変換したいときに呼ぶための関数
const formatBalance = setupCommaInput(balanceInput, () => refreshIfShowing("balance"));
const convertAccountNameToHankaku =
    setupHankakuInput(accountNameInput, () => refreshIfShowing("accountName"));

/* ============================================================================
 * 送信・リセット
 * ========================================================================== */

// 「登録」ボタンのclickではなくformのsubmitを見ることで、
// 入力欄でEnterキーを押して送信された場合もチェックが効くようにしている。
form.addEventListener("submit", (e) => {
    convertAccountNameToHankaku();      //  変換されないまま送信されるのを防ぐ
    let firstErrorRule = null;

    rules.forEach((rule) => {
        if (showResult(rule) !== "" && firstErrorRule === null) {
            firstErrorRule = rule;
        }
    });

    if (firstErrorRule !== null) {
        e.preventDefault();                         //  送信を中止する
        getFocusTarget(firstErrorRule).focus();     //  最初にエラーになった項目へカーソルを移す
        return;
    }
    balanceInput.value = toDigits(balanceInput.value);  //  サーバへは表示用のカンマを外した数字だけを送る
});

rules.forEach((rule) => {
    const errorArea = document.getElementById(rule.id + "_error");

    //  ラジオボタンは選択肢の数だけ input があるので、そのすべてに登録する
    getInputs(rule).forEach((input) => {
        // 入力途中で誤ったメッセージを出さないよう、
        // すでにエラーが出ている項目だけ入力のたびに再判定する（直したらすぐ消える）
        input.addEventListener("input", () => {
            if (errorArea.textContent !== "") {
                showResult(rule);
            }
        });

        // 入力を終えて別の項目へ移ったタイミング、ラジオボタンを選び直したタイミングで判定する
        input.addEventListener("change", () => showResult(rule));
    });
});

// 「クリア」ボタンで値を戻したときにエラー表示も消す。
// reset処理が終わったあとに実行したいので setTimeout で後ろにずらしている
form.addEventListener("reset", () => window.setTimeout(() => {
    clearAllErrors();
    resetBankBranchPickers();
    formatBalance();
}, 0));
