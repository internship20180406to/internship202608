document.addEventListener("DOMContentLoaded", () => {
    // 金融機関と支店の対応関係
    const branchOptions = {
        "山陰共同銀行": [
            "松江本店営業部",
            "鳥取支店",
            "米子支店"
        ],
        "カウカウ銀行": [
            "本店営業部",
            "福岡支店",
            "博多支店"
        ],
        "流れ星銀行": [
            "天神支店",
            "小倉支店",
            "久留米支店"
        ]
    };

    const bankInput =
        document.getElementById("bankName");

    const bankDataList =
        document.getElementById("bankNameOptions");

    const branchInput =
        document.getElementById("branchName");

    const branchDataList =
        document.getElementById("branchNameOptions");

    if (!bankInput || !bankDataList ||
        !branchInput || !branchDataList) {
        return;
    }

    // Controllerから渡された金融機関名を取得
    const bankNames =
        Array.from(bankDataList.options)
            .map((option) => option.value);

    // 金融機関名をチェック
    function validateBank() {
        const bankName = bankInput.value.trim();

        if (bankName === "") {
            bankInput.setCustomValidity("");
            return;
        }

        if (bankNames.includes(bankName)) {
            bankInput.setCustomValidity("");
        } else {
            bankInput.setCustomValidity(
                "候補から金融機関を選択してください"
            );
        }
    }

    // 支店名をチェック
    function validateBranch() {
        const bankName = bankInput.value.trim();
        const branchName = branchInput.value.trim();
        const branches = branchOptions[bankName] || [];

        if (branchName === "") {
            branchInput.setCustomValidity("");
            return;
        }

        if (branches.includes(branchName)) {
            branchInput.setCustomValidity("");
        } else {
            branchInput.setCustomValidity(
                "選択した金融機関の支店を候補から選択してください"
            );
        }
    }

    // 選択された金融機関に対応する支店候補を作る
    function updateBranchOptions() {
        const bankName = bankInput.value.trim();
        const branches = branchOptions[bankName] || [];

        branchDataList.innerHTML = "";

        branches.forEach((branchName) => {
            const option =
                document.createElement("option");

            option.value = branchName;
            branchDataList.appendChild(option);
        });

        // 現在の支店が選択した銀行に存在しなければ消す
        if (
            branchInput.value !== "" &&
            !branches.includes(branchInput.value)
        ) {
            branchInput.value = "";
        }

        validateBranch();
    }

    // 金融機関名が入力・選択されたとき
    bankInput.addEventListener("input", () => {
        validateBank();
        updateBranchOptions();
    });

    // 支店名が入力・選択されたとき
    branchInput.addEventListener("input", () => {
        validateBranch();
    });

    // クリアボタンが押されたとき
    const form = bankInput.closest("form");

    if (form) {
        form.addEventListener("reset", () => {
            setTimeout(() => {
                bankInput.setCustomValidity("");
                branchInput.setCustomValidity("");
                updateBranchOptions();
            }, 0);
        });
    }

    // 確認画面から戻った場合の値も復元する
    validateBank();
    updateBranchOptions();
});