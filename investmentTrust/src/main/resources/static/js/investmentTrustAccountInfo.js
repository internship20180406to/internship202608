// 投資信託 お客様情報・口座情報入力画面(investmentTrustMain.html)のうち、口座情報部分の動作を制御するJS
// 支店の絞り込み・口座番号の整形を担当する

const institutionSelect = document.getElementById("institutionCode");
const branchSelect = document.getElementById("branchCode");
const bankAccountNumField = document.getElementById("bankAccountNum");

// 選択された金融機関コードに紐づく支店だけを支店selectに反映する
function populateBranchOptions(institutionCode, selectedBranchCode) {
    branchSelect.innerHTML = "";

    if (!institutionCode) {
        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "金融機関を選択してください";
        branchSelect.appendChild(placeholder);
        return;
    }

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "選択してください";
    branchSelect.appendChild(placeholder);

    branchMasterList
        .filter(function (branch) { return branch.institutionCode === institutionCode; })
        .forEach(function (branch) {
            const option = document.createElement("option");
            option.value = branch.branchCode;
            option.textContent = branch.branchName;
            if (branch.branchCode === selectedBranchCode) {
                option.selected = true;
            }
            branchSelect.appendChild(option);
        });
}

institutionSelect.addEventListener("change", function () {
    populateBranchOptions(institutionSelect.value, null);
});

// 「入力内容をクリア」が押されたとき: ブラウザのリセット処理で金融機関が空欄に戻った直後に、
// 支店の選択肢も金融機関未選択の状態に合わせて作り直す(そのままだと前に選んでいた金融機関の支店一覧が残ってしまう)
const accountInfoForm = institutionSelect.closest("form");
if (accountInfoForm) {
    accountInfoForm.addEventListener("reset", function () {
        setTimeout(function () { populateBranchOptions(institutionSelect.value, null); }, 0);
    });
}

// 口座番号は数字以外の入力を除去し、7桁を超えたら切り捨てる(数字以外が入力された場合はその場でエラーを表示する)
bankAccountNumField.addEventListener("input", function () {
    const beforeLength = bankAccountNumField.value.length;
    const digitsOnly = bankAccountNumField.value.replace(/[^0-9]/g, "");
    bankAccountNumField.value = digitsOnly.slice(0, 7);

    showFieldError("bankAccountNum", digitsOnly.length !== beforeLength ? "口座番号は半角数字のみ入力してください" : "");
});

// 入力欄から離れたとき、7桁に満たなければ先頭を0で埋める(例: 444 → 0000444)
bankAccountNumField.addEventListener("blur", function () {
    const digits = bankAccountNumField.value;
    if (digits.length > 0 && digits.length < 7) {
        bankAccountNumField.value = digits.padStart(7, "0");
        updateFieldCheck("bankAccountNum", bankAccountNumField);
    }
    showFieldError("bankAccountNum", "");
});

// 初期表示(前の画面から戻ってきた場合の支店選択肢の復元を含む)
populateBranchOptions(institutionSelect.value, initialBranchCode);
