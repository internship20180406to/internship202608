/*
 * 金融機関コード・支店コードの入力部品（共通）。
 *
 * 「コードを打つと候補が下に並び、候補をクリックして『選択』ボタンで確定する」という
 * 一連の動きをまとめたもの。投資信託の申込画面と口座登録画面の両方から使う。
 *
 * ★画面側に用意しておく要素のid（この名前で探しにいく）:
 *     bankCode            金融機関コードの入力欄
 *     bankCodeSelect      金融機関の「選択」ボタン
 *     bankName_view       確定した金融機関名の表示先
 *     bankCode_candidates 金融機関の候補一覧を描く場所
 *     branchCode / branchCodeSelect / branchName_view / branchCode_candidates  （支店も同じ並び）
 *
 * ★ここで呼んでいるAPI（/api/banks/...）はあくまで入力支援。
 *   「APIが200を返したから正しい」とは考えないこと。JSは開発者ツールで無効化でき、
 *   APIを一度も呼ばずに直接POSTすることもできる。
 *   実在するコードかどうかの最終判定は、必ずサーバ側で行っている。
 */

const BANK_CODE_PATTERN = /^[0-9]{4}$/;     //  金融機関コードは半角数字4桁ちょうど
const BRANCH_CODE_PATTERN = /^[0-9]{3}$/;   //  支店コードは半角数字3桁ちょうど

/*
 * 問い合わせの結果は、入力欄の data-found 属性に持たせている。
 *   未設定 … まだ問い合わせていない
 *   "1"    … 見つかった
 *   "0"    … 見つからなかった
 *
 * 真偽値ではなく3状態にしているのは「まだ問い合わせていない」を区別するため。
 * 未問い合わせのまま送信された場合、フロントでは判定せずサーバに任せる
 * （フロントで勝手にエラーにすると、通信が遅いだけで送信できなくなってしまう）。
 */
const bankBranchNotFound = (id) => document.getElementById(id).dataset.found === "0";

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

/** 名称表示を消し、「まだ確定していない」状態に戻す */
const clearPickerName = (id) => {
    delete document.getElementById(id).dataset.found;
    document.getElementById(PICKERS[id].nameViewId).textContent = "";
};

/** 両方のコード欄を未確定に戻す（クリアボタン用） */
const resetBankBranchPickers = () => {
    clearPickerName("bankCode");
    clearPickerName("branchCode");
    resetBranchCandidates();
};

/*
 * 取り寄せた候補を覚えておく。キーを打つたびにサーバへ問い合わせないための簡易キャッシュ。
 * 支店は金融機関ごとに変わるので、どの金融機関の分を持っているかも覚えておく。
 */
let bankCandidates = null;              //  null = まだ取り寄せていない
let branchCandidates = null;
let branchCandidatesBankCode = "";

/** 候補一覧は「コードと表示名」だけあれば描けるので、この形に揃える */
const toCandidate = (code, label) => ({code: code, label: label});

/** 金融機関が変わったら、覚えている支店の候補を捨てる */
const resetBranchCandidates = () => {
    branchCandidates = null;
    branchCandidatesBankCode = "";
};

const loadBankCandidates = () => {
    if (bankCandidates !== null) {
        return Promise.resolve(bankCandidates);
    }
    return fetch("/api/banks")
        .then((response) => response.json())
        .then((banks) => {
            bankCandidates = banks.map((bank) => toCandidate(bank.bankCode, bank.bankName));
            return bankCandidates;
        });
};

/** 支店の候補。金融機関コードが決まっていないと候補を出せない */
const loadBranchCandidates = () => {
    const bankCode = document.getElementById("bankCode").value.trim();
    if (!BANK_CODE_PATTERN.test(bankCode)) {
        return Promise.resolve([]);
    }
    if (branchCandidates !== null && branchCandidatesBankCode === bankCode) {
        return Promise.resolve(branchCandidates);
    }
    return fetch("/api/banks/" + encodeURIComponent(bankCode) + "/branches")
        .then((response) => response.json())
        .then((branches) => {
            branchCandidates = branches.map((branch) => toCandidate(branch.branchCode, branch.branchName));
            branchCandidatesBankCode = bankCode;
            return branchCandidates;
        });
};

/*
 * 金融機関と支店で違うのは「候補の取り寄せ方」と「1件を引く先のURL」だけなので、
 * その差分だけをここにまとめ、一覧の描画や選択の処理は共通の関数で扱う。
 */
const PICKERS = {
    bankCode: {
        pattern: BANK_CODE_PATTERN,
        nameViewId: "bankName_view",
        loadCandidates: loadBankCandidates,
        emptyMessage: () => "該当する金融機関がありません。",
        lookupName: (code) => fetch("/api/banks/" + encodeURIComponent(code))
            .then(toJsonOrNull)
            .then((bank) => (bank === null) ? null : bank.bankName)
    },
    branchCode: {
        pattern: BRANCH_CODE_PATTERN,
        nameViewId: "branchName_view",
        loadCandidates: loadBranchCandidates,
        //  支店の候補が空になる理由は2つあるので、メッセージを出し分ける
        emptyMessage: () => BANK_CODE_PATTERN.test(document.getElementById("bankCode").value.trim())
            ? "該当する支店がありません。"
            : "先に金融機関コードを選択してください。",
        lookupName: (code) => fetch("/api/banks/"
            + encodeURIComponent(document.getElementById("bankCode").value.trim())
            + "/branches/" + encodeURIComponent(code))
            .then(toJsonOrNull)
            .then((branch) => (branch === null) ? null : branch.branchName)
    }
};

/** 候補一覧を閉じる */
const hideCandidates = (id) => {
    const area = document.getElementById(id + "_candidates");
    area.classList.add("hidden");
    area.textContent = "";
};

/** 候補一覧を描き直して表示する */
const renderCandidates = (id, candidates) => {
    const area = document.getElementById(id + "_candidates");
    area.textContent = "";      //  いったん空にしてから作り直す

    if (candidates.length === 0) {
        const empty = document.createElement("span");
        empty.className = "candidate-empty";
        empty.textContent = PICKERS[id].emptyMessage();
        area.appendChild(empty);
    } else {
        const selectedCode = document.getElementById(id).value.trim();
        candidates.forEach((candidate) => {
            const item = document.createElement("span");
            item.className = (candidate.code === selectedCode) ? "candidate selected" : "candidate";
            item.dataset.code = candidate.code;
            item.textContent = candidate.code + " " + candidate.label;
            //  click ではなく mousedown で拾う。
            //  click だと先に入力欄のblurが起きて一覧が閉じ、クリックが届かなくなるため。
            //  preventDefault でフォーカスが外れるのも防いでいる。
            item.addEventListener("mousedown", (event) => {
                event.preventDefault();
                selectCandidate(id, candidate.code);
            });
            area.appendChild(item);
        });
    }
    area.classList.remove("hidden");
};

/**
 * 入力中のコードを先頭一致で絞り込んで候補を表示する。
 *
 * 候補は一度まとめて取り寄せ、絞り込みはブラウザ側で行っている。
 * 今のマスタは十数件なのでこれで十分だが、件数が増えたら
 * サーバ側で絞り込む（/api/banks?prefix=00 のような形にする）ことになる。
 */
const showCandidates = (id) => {
    PICKERS[id].loadCandidates()
        .then((candidates) => {
            const prefix = document.getElementById(id).value.trim();
            renderCandidates(id, candidates.filter((candidate) => candidate.code.startsWith(prefix)));
        })
        .catch(() => hideCandidates(id));
};

/*
 * 画面側へ知らせるためのコールバック。setupBankBranchPickers で受け取る。
 *
 * 2つに分けているのが要点。
 *   onConfirmed … 「選択」ボタンで確定したとき。結果を必ず表示する
 *   onEditing   … 入力中や候補をクリックしただけのとき。
 *                 すでにエラーが出ている項目だけ掛け直す
 *
 * 1つにまとめて常に判定すると、コードを1文字打った時点で
 * 「4桁で入力してください」と出てしまい、入力の邪魔になる。
 */
let onConfirmed = () => {
};
let onEditing = () => {
};

/** 候補をクリックしたとき:コード入力欄に入れて、選択中であることを示す */
const selectCandidate = (id, code) => {
    document.getElementById(id).value = code;
    //  クリックしただけではまだ確定ではない。名称は「選択」を押すまで出さない
    clearPickerName(id);
    document.getElementById(id + "_candidates").querySelectorAll(".candidate")
        .forEach((item) => item.classList.toggle("selected", item.dataset.code === code));
    onEditing(id);
};

/**
 * 金融機関が確定したあとに、支店の確定内容を見直す。
 *
 * 金融機関が変わると、同じ支店コードでも別の支店になったり、存在しなくなったりする。
 * かといって支店を無条件に消してしまうと、
 *   ・同じ金融機関を選び直しただけでも、確定済みの支店名が消える
 *   ・支店をまだ入力していない段階で「支店コードを入力してください」と出てしまう
 * ということが起きる。
 * そこで「支店コードが入っているときだけ、新しい金融機関で引き直す」形にしている。
 */
const revalidateBranch = () => {
    resetBranchCandidates();    //  候補は金融機関ごとに違うので捨てる

    const code = document.getElementById("branchCode").value.trim();
    if (!BRANCH_CODE_PATTERN.test(code)) {
        //  まだ支店を入力していない。ここでエラーを出すのは早すぎるので、
        //  表示を消すだけにして判定は掛けない
        clearPickerName("branchCode");
        onEditing("branchCode");
        return;
    }
    //  支店コードは入っている。新しい金融機関でも同じ支店が存在するか確かめ直す。
    //  引き直す前の名称はあえて消さない（結果が返るまでの一瞬だけ前の表示が残る）
    confirmSelection("branchCode");
};

/** 「選択」ボタン:入力欄のコードを確定し、名称を表示する */
const confirmSelection = (id) => {
    const picker = PICKERS[id];
    const input = document.getElementById(id);
    const code = input.value.trim();

    if (!picker.pattern.test(code)) {
        clearPickerName(id);
        onConfirmed(id);    //  未入力・桁数違いをその場で知らせる
        return;
    }
    picker.lookupName(code)
        .then((name) => {
            input.dataset.found = (name === null) ? "0" : "1";
            document.getElementById(picker.nameViewId).textContent = (name === null) ? "" : name;
            hideCandidates(id);
            onConfirmed(id);
            if (id === "bankCode") {
                revalidateBranch();
            }
        })
        //  通信できないときは「分からない」状態に戻し、判定はサーバに任せる
        .catch(() => clearPickerName(id));
};

/**
 * 画面の読み込み後に1回呼ぶ。
 *
 * @param callbacks 画面側の入力チェックを掛け直すための関数を2つ渡す
 *        confirmed … 「選択」で確定したとき（結果を必ず表示する）
 *        editing   … 入力中・候補をクリックしただけのとき
 *                    （すでにエラーが出ている項目だけ掛け直す）
 */
const setupBankBranchPickers = (callbacks) => {
    onConfirmed = callbacks.confirmed;
    onEditing = callbacks.editing;

    ["bankCode", "branchCode"].forEach((id) => {
        const input = document.getElementById(id);

        //  入力の途中で前に確定した名称が残っていると誤解を招くので、値が変わった時点で消す
        input.addEventListener("input", () => {
            clearPickerName(id);
            showCandidates(id);
            onEditing(id);
        });
        input.addEventListener("focus", () => showCandidates(id));
        input.addEventListener("blur", () => hideCandidates(id));

        //  確定するのは「選択」ボタンを押したときだけ。
        //  入力欄を離れただけで確定させると、候補をクリックした瞬間に確定してしまう。
        document.getElementById(id + "Select").addEventListener("click", () => confirmSelection(id));
    });
};
