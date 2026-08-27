/*
 * チャット形式の投資信託申込画面。
 *
 * フォーム版（inputConfirmation.js）との関係:
 *   ・入力項目も送信する値も同じ。違うのは「どう集めるか」だけ
 *   ・半角カナ変換（hankakuKana.js）とカンマ整形（numberFormat.js）は同じ部品を使う
 *   ・実在するコードか、口座があるか、残高が足りるかの最終判定はサーバが行う。
 *     ここでの判定は入力支援であり、JSを無効化されても
 *     POST /api/investmentTrust/order 側で必ず弾かれる
 *
 * 使うAPI:
 *   GET  /api/banks                                 金融機関の一覧（候補用）
 *   GET  /api/banks/{bankCode}                      金融機関1件
 *   GET  /api/banks/{bankCode}/branches             支店の一覧（候補用）
 *   GET  /api/banks/{bankCode}/branches/{code}      支店1件
 *   GET  /api/accounts?...                          口座の名義と残高
 *   POST /api/investmentTrust/order                 申込の確定
 */

const OPTIONS_EL = document.getElementById("chatOptions");
const ACCOUNT_TYPES = OPTIONS_EL.dataset.types.split(",");
const FUND_NAMES = OPTIONS_EL.dataset.funds.split(",");

/*
 * 会話の順番と、項目ごとの決まりごと。
 *   kind … code:コード入力＋候補 / choice:選択ボタン / text・money:入力欄
 *   fix  … 入力のたびに値を整える方法（digits / kana / comma）
 *   max  … 桁数の上限。ここで頭打ちにするので8桁目が打てない
 */
const STEPS = [
    {
        key: "bankCode", label: "金融機関", kind: "code", fix: "digits", max: 4, mode: "numeric",
        prompt: "投資信託のお申込みを承ります。\nはじめに、金融機関コードを4桁の数字で入力してください。",
        hint: "半角数字4桁。下の候補から選ぶこともできます。", placeholder: "0001"
    },
    {
        key: "branchCode", label: "支店", kind: "code", fix: "digits", max: 3, mode: "numeric",
        prompt: "次に、支店コードを3桁の数字で入力してください。",
        hint: "半角数字3桁。下の候補から選ぶこともできます。", placeholder: "002"
    },
    {
        key: "bankAccountType", label: "科目", kind: "choice", choices: ACCOUNT_TYPES,
        prompt: "口座の科目を選んでください。"
    },
    {
        key: "bankAccountNum", label: "口座番号", kind: "text", fix: "digits", max: 7, mode: "numeric",
        prompt: "口座番号を7桁の数字で入力してください。",
        hint: "半角数字7桁。先頭の0も含めて入力してください。", placeholder: "0031111"
    },
    {
        key: "name", label: "購入者名", kind: "text", fix: "kana", max: 20,
        prompt: "購入者名を入力してください。",
        hint: "かな・全角カナで打っても半角カナに変換されます（例: やまだ → ﾔﾏﾀﾞ）。", placeholder: "ﾔﾏﾀﾞ ﾀﾛｳ"
    },
    {
        key: "fundName", label: "銘柄", kind: "choice", choices: FUND_NAMES,
        prompt: "購入する銘柄を選んでください。"
    },
    {
        key: "money", label: "金額", kind: "money", fix: "comma", max: 12, mode: "numeric",
        prompt: "購入金額を入力してください。",
        hint: "10,000円以上 10,000,000円以下。3桁ごとに自動でカンマが入ります。", placeholder: "50,000"
    }
];

const MONEY_MIN = 10000;
const MONEY_MAX = 10000000;

const transcriptEl = document.getElementById("transcript");
const composerEl = document.getElementById("composer");
const progressLabelEl = document.getElementById("progressLabel");
const progressBarEl = document.getElementById("progressBar");

/*
 * 画面の状態。
 *   answers … 確定した回答。key は STEPS の key と同じでサーバの項目名にも一致する
 *   queue   … これから順番に出す応答。打っている演出のために1つずつ出す
 *   names   … コードから引いた名称。サマリー表示にだけ使い、送信はしない
 */
const state = {
    step: 0, answers: {}, names: {}, queue: [], typing: false,
    done: false, sending: false, busy: false
};
let typingTimer = null;

/* ============================================================================
 * 吹き出しの描画
 * ========================================================================== */

const yen = (n) => Number(n).toLocaleString("ja-JP") + "円";

/** 追加した要素まで自動で送る。これが無いと質問が下に隠れたまま気づかれない */
const scrollToBottom = () => {
    transcriptEl.scrollTop = transcriptEl.scrollHeight;
};

const addBotBubble = (text) => {
    const row = document.createElement("div");
    row.className = "row_bot";
    const bubble = document.createElement("div");
    bubble.className = "bubble_bot";
    bubble.textContent = text;
    row.appendChild(bubble);
    transcriptEl.appendChild(row);
    scrollToBottom();
};

const addErrorBubble = (text) => {
    const row = document.createElement("div");
    row.className = "row_bot";
    const bubble = document.createElement("div");
    bubble.className = "bubble_error";
    bubble.textContent = text;
    row.appendChild(bubble);
    transcriptEl.appendChild(row);
    scrollToBottom();
};

/**
 * 利用者の回答。step を渡したものだけ、あとから「編集」できる。
 * 弾かれた入力や操作の吹き出しには編集ボタンを付けない。
 */
const addUserBubble = (text, step) => {
    const row = document.createElement("div");
    row.className = "row_user";
    if (step !== undefined && step !== null) {
        row.dataset.step = String(step);
        const edit = document.createElement("button");
        edit.type = "button";
        edit.className = "btn btn_secondary btn_small edit_button";
        edit.textContent = "編集";
        edit.addEventListener("click", () => editItem(step));
        row.appendChild(edit);
    }
    const bubble = document.createElement("div");
    bubble.className = "bubble_user";
    bubble.textContent = text;
    row.appendChild(bubble);
    transcriptEl.appendChild(row);
    scrollToBottom();
};

/**
 * 「今も有効な回答」だけを編集できるようにする。
 * 巻き戻して無効になった回答や、同じ項目に answer し直して古くなった吹き出しからは
 * 編集ボタンを消す（最後のものだけ残す）。
 */
const refreshEditButtons = () => {
    const rows = Array.from(transcriptEl.querySelectorAll(".row_user[data-step]"));
    const latest = {};
    rows.forEach((row) => {
        latest[row.dataset.step] = row;
    });
    rows.forEach((row) => {
        const step = Number(row.dataset.step);
        const alive = !state.done && step < state.step && latest[row.dataset.step] === row;
        const button = row.querySelector(".edit_button");
        if (button) {
            button.style.display = alive ? "" : "none";
        }
    });
};

/* ============================================================================
 * 応答を1つずつ出す（打っている演出）
 *
 * 応答をまとめて出さず、文字数に応じた間を置いて順番に出す。
 * この間は入力欄も選択肢も出さない。質問が届く前に答えられる状態を作らないため。
 * API待ちの間も同じ表示を使うので、通信中であることも同時に伝わる。
 * ========================================================================== */

const showTyping = () => {
    if (state.typing) {
        return;
    }
    state.typing = true;
    const row = document.createElement("div");
    row.className = "row_bot";
    row.id = "typingRow";
    const box = document.createElement("div");
    box.className = "typing";
    box.appendChild(document.createElement("span"));
    box.appendChild(document.createElement("span"));
    box.appendChild(document.createElement("span"));
    row.appendChild(box);
    transcriptEl.appendChild(row);
    scrollToBottom();
};

const hideTyping = () => {
    state.typing = false;
    const row = document.getElementById("typingRow");
    if (row) {
        row.remove();
    }
};

/** 応答を積む。積んだ順に出る */
const queueBot = (text) => state.queue.push({ kind: "bot", text: text });
const queueError = (text) => state.queue.push({ kind: "error", text: text });

const pump = () => {
    if (typingTimer) {
        return;
    }
    if (state.queue.length === 0) {
        hideTyping();
        state.busy = false;
        renderComposer();
        return;
    }
    const next = state.queue.shift();
    //  文字数に応じて間を変える。固定秒だと短い相槌にも同じだけ待たされて機械的に見える
    const wait = 380 + Math.min(1000, next.text.length * 26);
    showTyping();
    renderComposer();
    typingTimer = window.setTimeout(() => {
        typingTimer = null;
        hideTyping();
        if (next.kind === "error") {
            addErrorBubble(next.text);
        } else {
            addBotBubble(next.text);
        }
        pump();
    }, wait);
};

/** 応答を積んだうえで、出し終わるまで入力を止める */
const flush = () => {
    state.busy = true;
    renderComposer();
    pump();
};

/* ============================================================================
 * 会話を進める
 * ========================================================================== */

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

/** 入力を弾く。会話は消さず、同じ質問に留まる */
const reject = (value, message) => {
    addUserBubble(value === "" ? "（未入力）" : value, null);
    queueError(message);
    flush();
};

/** 回答を受け付けて次の質問へ進む */
const advance = (value, acks) => {
    const step = STEPS[state.step];
    state.answers[step.key] = value;
    addUserBubble(String(value), state.step);
    state.step = state.step + 1;
    refreshEditButtons();
    acks.forEach((a) => queueBot(a));
    if (state.step < STEPS.length) {
        queueBot(STEPS[state.step].prompt);
    } else {
        queueBot("ありがとうございます。内容をご確認ください。");
    }
    flush();
};

/**
 * 入力された値を判定して会話を進める。
 * コード・口座はサーバに問い合わせるので非同期になる。
 */
const accept = (raw) => {
    if (state.busy || state.done) {
        return;
    }
    const step = STEPS[state.step];
    const value = String(raw == null ? "" : raw).trim();

    if (step.key === "bankCode") {
        if (!/^[0-9]{4}$/.test(value)) {
            return reject(value, "金融機関コードは半角数字4桁で入力してください。");
        }
        state.busy = true;
        showTyping();
        renderComposer();
        return fetch("/api/banks/" + encodeURIComponent(value)).then(toJsonOrNull)
            .then((bank) => {
                hideTyping();
                if (bank === null) {
                    return reject(value, "該当する金融機関がありません。コードをご確認ください。");
                }
                state.names.bankCode = bank.bankName;
                advance(value, [bank.bankCode + " " + bank.bankName + " ですね。"]);
            })
            .catch(() => {
                hideTyping();
                reject(value, "金融機関を確認できませんでした。通信の状態をご確認のうえ、もう一度お試しください。");
            });
    }

    if (step.key === "branchCode") {
        if (!/^[0-9]{3}$/.test(value)) {
            return reject(value, "支店コードは半角数字3桁で入力してください。");
        }
        state.busy = true;
        showTyping();
        renderComposer();
        const url = "/api/banks/" + encodeURIComponent(state.answers.bankCode)
            + "/branches/" + encodeURIComponent(value);
        return fetch(url).then(toJsonOrNull)
            .then((branch) => {
                hideTyping();
                if (branch === null) {
                    return reject(value, "この金融機関にその支店コードはありません。\n支店コードは金融機関ごとに振られています。");
                }
                state.names.branchCode = branch.branchName;
                advance(value, [branch.branchCode + " " + branch.branchName + " ですね。"]);
            })
            .catch(() => {
                hideTyping();
                reject(value, "支店を確認できませんでした。通信の状態をご確認のうえ、もう一度お試しください。");
            });
    }

    if (step.key === "bankAccountNum") {
        if (!/^[0-9]{7}$/.test(value)) {
            return reject(value, "口座番号は半角数字7桁で入力してください。");
        }
        state.busy = true;
        showTyping();
        renderComposer();
        const query = new URLSearchParams({
            bankCode: state.answers.bankCode, branchCode: state.answers.branchCode,
            accountType: state.answers.bankAccountType, accountNum: value
        });
        return fetch("/api/accounts?" + query.toString()).then(toJsonOrNull)
            .then((account) => {
                hideTyping();
                if (account === null) {
                    //  4項目の組み合わせに対するエラー。口座番号だけが違うとは限らないので、
                    //  フォーム版の全体エラーと同じ文言にそろえている
                    return reject(value, "この口座は登録されていません。\n金融機関・支店・科目・口座番号の組み合わせをご確認ください。");
                }
                state.names.balance = account.balance;
                advance(value, ["ご名義は " + account.accountName + " 様、現在の残高は "
                    + yen(account.balance) + " です。"]);
            })
            .catch(() => {
                hideTyping();
                reject(value, "口座を確認できませんでした。通信の状態をご確認のうえ、もう一度お試しください。");
            });
    }

    if (step.key === "name") {
        if (!KANA_PATTERN.test(value)) {
            return reject(value, "購入者名は半角カナ（半角スペース可）で入力してください。");
        }
        if (value.length > 20) {
            return reject(value, "購入者名は20文字以内で入力してください。");
        }
        return advance(value, []);
    }

    if (step.key === "money") {
        const digits = toDigits(value);
        if (digits === "" || !MONEY_PATTERN.test(value)) {
            return reject(value, "金額は半角数字（1円単位）で入力してください。");
        }
        const amount = Number(digits);
        if (amount < MONEY_MIN) {
            return reject(value, "金額は" + MONEY_MIN.toLocaleString() + "円以上で入力してください。");
        }
        if (amount > MONEY_MAX) {
            return reject(value, "金額は" + MONEY_MAX.toLocaleString() + "円以下で入力してください。");
        }
        if (state.names.balance !== undefined && amount > state.names.balance) {
            return reject(value, "残高が不足しています。（残高: " + yen(state.names.balance) + "）");
        }
        //  サーバへは表示用のカンマを外した数字だけを送る
        state.answers.money = amount;
        addUserBubble(yen(amount), state.step);
        state.step = STEPS.length;
        refreshEditButtons();
        queueBot("ありがとうございます。内容をご確認ください。");
        return flush();
    }

    //  科目・銘柄は選択ボタンなので書式の判定は要らない
    return advance(value, []);
};

/** 「編集」: その項目まで戻し、以降の回答だけを捨てる */
const editItem = (index) => {
    if (state.busy || state.done) {
        return;
    }
    STEPS.slice(index).forEach((s) => {
        delete state.answers[s.key];
    });
    if (index <= 3) {
        delete state.names.balance;   //  口座が変わりうるので残高の記憶も捨てる
    }
    state.step = index;
    refreshEditButtons();
    queueBot(STEPS[index].label + "を入力し直します。\n" + STEPS[index].prompt);
    flush();
};

/* ============================================================================
 * 申込の確定
 * ========================================================================== */

const submitOrder = (button) => {
    if (state.sending || state.done) {
        return;
    }
    //  fetchで送るので再読み込みによる再送は起きない。
    //  残るのは連打なので、送信中はボタンを止める
    state.sending = true;
    button.disabled = true;
    button.textContent = "送信中…";

    fetch("/api/investmentTrust/order", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(state.answers)
    })
        .then((response) => response.json().then((body) => ({ ok: response.ok, body: body })))
        .then((result) => {
            state.sending = false;
            addUserBubble("この内容で申し込む", null);
            if (result.ok) {
                state.done = true;
                refreshEditButtons();
                queueBot("お申込みを受け付けました。\n引き落とし後の残高は "
                    + yen(result.body.balanceAfter) + " です。");
                flush();
                return;
            }
            //  会話の途中で確認していても、そこから申込までの間に残高が減ることがある。
            //  最終的な判定はサーバなので、返ってきた内容をそのまま会話に出す。
            const fieldErrors = result.body.fieldErrors || {};
            const globalErrors = result.body.globalErrors || [];
            globalErrors.forEach((m) => queueError(m));
            Object.keys(fieldErrors).forEach((key) => queueError(fieldErrors[key]));
            if (globalErrors.length === 0 && Object.keys(fieldErrors).length === 0) {
                queueError("お申込みを受け付けられませんでした。内容をご確認ください。");
            }
            //  どの項目が問題かはサーバの返答で分かるので、その項目まで戻す
            const firstKey = Object.keys(fieldErrors)[0];
            const backTo = STEPS.findIndex((s) => s.key === firstKey);
            state.step = (backTo >= 0) ? backTo : STEPS.length;
            if (backTo >= 0) {
                STEPS.slice(backTo).forEach((s) => delete state.answers[s.key]);
                queueBot(STEPS[backTo].prompt);
            }
            refreshEditButtons();
            flush();
        })
        .catch(() => {
            state.sending = false;
            addUserBubble("この内容で申し込む", null);
            queueError("お申込みを送信できませんでした。\n通信の状態をご確認のうえ、もう一度お試しください。");
            flush();
        });
};

/* ============================================================================
 * 入力欄・選択肢・サマリーの描画
 * ========================================================================== */

/** 数字以外を受け付けない。桁数もここで頭打ちにする（カーソル位置は保つ） */
const enforceDigits = (input, max) => {
    const before = input.value;
    const caret = input.selectionStart;
    const digitsBeforeCaret = (caret === null) ? -1 : before.slice(0, caret).replace(/[^0-9]/g, "").length;
    const after = before.replace(/[^0-9]/g, "").slice(0, max);
    if (after === before) {
        return;
    }
    input.value = after;
    if (digitsBeforeCaret >= 0 && document.activeElement === input) {
        const position = Math.min(digitsBeforeCaret, after.length);
        input.setSelectionRange(position, position);
    }
};

/** 候補一覧を描く。金融機関・支店のコード入力のときだけ出る */
const renderCandidates = (area, step) => {
    const url = (step.key === "bankCode")
        ? "/api/banks"
        : "/api/banks/" + encodeURIComponent(state.answers.bankCode) + "/branches";
    fetch(url).then((r) => r.json()).then((list) => {
        const items = list.map((x) => (step.key === "bankCode")
            ? { code: x.bankCode, label: x.bankCode + "　" + x.bankName }
            : { code: x.branchCode, label: x.branchCode + "　" + x.branchName });
        area.dataset.loaded = "1";
        area.items = items;
        filterCandidates(area, "");
    }).catch(() => {
        area.textContent = "";
    });
};

/** 入力中のコードで候補を先頭一致で絞り込む。件数が少ないのでブラウザ側で絞っている */
const filterCandidates = (area, prefix) => {
    const items = area.items || [];
    area.textContent = "";
    const matched = items.filter((x) => x.code.indexOf(prefix) === 0);
    if (matched.length === 0) {
        const empty = document.createElement("span");
        empty.className = "candidate-empty";
        empty.textContent = (STEPS[state.step].key === "bankCode")
            ? "該当する金融機関がありません。" : "該当する支店がありません。";
        area.appendChild(empty);
        return;
    }
    matched.forEach((x) => {
        const item = document.createElement("span");
        item.className = "candidate";
        item.textContent = x.label;
        //  click だと入力欄のblurが先に起きるので mousedown で拾う
        item.addEventListener("mousedown", (e) => {
            e.preventDefault();
            accept(x.code);
        });
        area.appendChild(item);
    });
};

const button = (label, className, onClick) => {
    const b = document.createElement("button");
    b.type = "button";
    b.className = "btn " + className;
    b.textContent = label;
    b.addEventListener("click", () => onClick(b));
    return b;
};

/** サマリー。各行から直接その項目に戻れる */
const renderSummary = () => {
    const box = document.createElement("div");
    box.className = "summary";
    STEPS.forEach((step, i) => {
        const row = document.createElement("div");
        row.className = "summary_row";
        const label = document.createElement("span");
        label.className = "summary_label";
        label.textContent = step.label;
        const value = document.createElement("span");
        value.className = "summary_value";
        let shown = state.answers[step.key];
        if (step.key === "bankCode") { shown = shown + " " + (state.names.bankCode || ""); }
        if (step.key === "branchCode") { shown = shown + " " + (state.names.branchCode || ""); }
        if (step.key === "money") { shown = yen(shown); }
        value.textContent = shown;
        row.appendChild(label);
        row.appendChild(value);
        row.appendChild(button("修正", "btn_secondary btn_small", () => editItem(i)));
        box.appendChild(row);
    });
    composerEl.appendChild(box);

    const actions = document.createElement("div");
    actions.className = "actions";
    actions.appendChild(button("最初から", "btn_secondary", () => restart()));
    actions.appendChild(button("この内容で申し込む", "btn_primary", (b) => submitOrder(b)));
    composerEl.appendChild(actions);
};

/** 進捗の表示を更新する */
const renderProgress = () => {
    const filled = Math.min(state.step, STEPS.length);
    progressLabelEl.textContent = state.done ? "完了"
        : (state.step >= STEPS.length ? "内容の確認" : (filled + 1) + " / " + STEPS.length);
    progressBarEl.style.width = Math.round((filled / STEPS.length) * 100) + "%";
};

/**
 * 入力欄まわりを描き直す。
 * 応答を出している間（busy）は何も出さない。質問が届く前に答えられないようにするため。
 */
const renderComposer = () => {
    renderProgress();
    composerEl.textContent = "";
    if (state.busy) {
        return;
    }
    if (state.done) {
        const actions = document.createElement("div");
        actions.className = "actions";
        actions.appendChild(button("最初から", "btn_secondary", () => restart()));
        composerEl.appendChild(actions);
        return;
    }
    if (state.step >= STEPS.length) {
        renderSummary();
        return;
    }

    const step = STEPS[state.step];

    if (step.kind === "choice") {
        const box = document.createElement("div");
        box.className = "choices";
        step.choices.forEach((c) => {
            const b = button(c, "choice", () => accept(c));
            b.className = "choice";
            box.appendChild(b);
        });
        composerEl.appendChild(box);
        return;
    }

    const hint = document.createElement("span");
    hint.className = "form_hint";
    hint.textContent = step.hint;
    composerEl.appendChild(hint);

    const row = document.createElement("div");
    row.className = "composer_row";
    const input = document.createElement("input");
    input.type = "text";
    input.className = "chat_input";
    input.placeholder = step.placeholder;
    input.autocomplete = "off";
    input.maxLength = step.max;
    if (step.mode) {
        input.inputMode = step.mode;
    }
    row.appendChild(input);
    row.appendChild(button("送信", "btn_primary", () => accept(input.value)));
    composerEl.appendChild(row);

    let candidateArea = null;
    if (step.kind === "code") {
        candidateArea = document.createElement("div");
        candidateArea.className = "candidate-list";
        composerEl.appendChild(candidateArea);
        renderCandidates(candidateArea, step);
    }

    //  入力のたびに値を整える。フォーム版と同じ共通部品を使う
    if (step.fix === "kana") {
        setupHankakuInput(input, () => {});
    } else if (step.fix === "comma") {
        setupCommaInput(input, () => {});
    }
    input.addEventListener("input", () => {
        if (step.fix === "digits") {
            enforceDigits(input, step.max);
        }
        if (candidateArea && candidateArea.dataset.loaded === "1") {
            filterCandidates(candidateArea, input.value.trim());
        }
    });
    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            accept(input.value);
        }
    });
    input.focus();
};

const restart = () => {
    if (typingTimer) {
        window.clearTimeout(typingTimer);
        typingTimer = null;
    }
    state.step = 0;
    state.answers = {};
    state.names = {};
    state.queue = [];
    state.done = false;
    state.sending = false;
    state.busy = false;
    hideTyping();
    transcriptEl.textContent = "";
    queueBot(STEPS[0].prompt);
    flush();
};

//  最初の問いかけも queue に入れておくと、開いた直後から打っている演出になる
queueBot(STEPS[0].prompt);
flush();
