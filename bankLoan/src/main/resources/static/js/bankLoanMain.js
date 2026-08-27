// ==============================
// HTML要素の取得
// ==============================

const bankNameSelect = document.getElementById('bankName');
const branchNameSelect = document.getElementById('branchName');
const branchSearch = document.getElementById('branchSearch');
const selectedBranchInput = document.getElementById('selectedBranch');

const bankAccountTypeInput =
    document.getElementById('bankAccountType');

const bankAccountNumInput =
    document.getElementById('bankAccountNum');


const loanAmountInput =
    document.getElementById('loanAmount');

const annualIncomeInput =
    document.getElementById('annualIncome');

const interestRateInput =
    document.getElementById('interestRate');

const loanLimit =
    document.getElementById('loanLimit');

const lastNameInput =
    document.getElementById('lastName');

const firstNameInput =
    document.getElementById('firstName');

const lastNameKanaInput =
    document.getElementById('lastNameKana');

const firstNameKanaInput =
    document.getElementById('firstNameKana');

const birthDateInput =
    document.getElementById('birthDate');

// ==============================
// クライアントエラー表示
// ==============================

const bankNameError =
    document.getElementById('bankNameClientError');

const branchNameError =
    document.getElementById('branchNameClientError');

const bankAccountTypeError =
    document.getElementById('bankAccountTypeClientError');

const bankAccountNumError =
    document.getElementById('bankAccountNumClientError');

const loanAmountError =
    document.getElementById('loanAmountClientError');

const annualIncomeError =
    document.getElementById('annualIncomeClientError');

const interestRateError =
    document.getElementById('interestRateClientError');

const loanLimitError =
    document.getElementById('loanLimitClientError');

const lastNameError =
    document.getElementById('lastNameClientError');

const firstNameError =
    document.getElementById('firstNameClientError');

const lastNameKanaError =
    document.getElementById('lastNameKanaClientError');

const firstNameKanaError =
    document.getElementById('firstNameKanaClientError');

const birthDateError =
    document.getElementById('birthDateClientError');


// ==============================
// Spring側エラー
// ==============================

const loanLimitServerError =
    document.getElementById('loanLimitServerError');

const birthDateServerError =
    document.getElementById('birthDateServerError');

const ageServerError =
    document.getElementById('ageServerError');

const annualIncome = document.getElementById('annualIncome');

const branchNameInput = document.getElementById('branchName');
const loanAmount = document.getElementById('loanAmount');

// ==============================
// 無操作タイムアウト用
// ==============================
const TIMEOUT_TIME = 10 * 60 * 1000;
const WARNING_TIME = 8 * 60 * 1000;
const sessionTimer =
    document.getElementById('sessionTimer');

let warningTimer;
let timeoutTimer;
let timeoutDeadline;
let countdownInterval;

function calculateLoanLimit() {
    const income = Number(annualIncomeInput.value);

    if (annualIncomeInput.value === '' || Number.isNaN(income)) {
        return 10;
    }

    // 年収の50％を10万円単位で切り捨て
    let limit = Math.floor((income * 0.5) / 10) * 10;

    // 最低限度額を10万円にする
    if (limit < 10) {
        limit = 10;
    }

    // カードローンの上限を1000万円とする場合
    if (limit > 1000) {
        limit = 1000;
    }

    return limit;
}

function clearForm() {
    const form = document.querySelector('form');

    form.querySelectorAll('input[type="text"], input[type="number"]').forEach(input => {
        input.value = '';
    });

    form.querySelectorAll('select').forEach(select => {
        select.selectedIndex = 0;
    });

    form.querySelectorAll('.server-error').forEach(error => {
        error.textContent = '';
    });

    form.querySelectorAll('.client-error').forEach(error => {
        error.textContent = '';
    });

    const loanLimit = document.getElementById('loanLimit');
    if (loanLimit) {
        loanLimit.textContent = '借入限度額：10万円';
    }

    const selectedBranch = document.getElementById('selectedBranch');
    if (selectedBranch) {
        selectedBranch.value = '';
    }

    // 金融機関未選択用の支店表示に戻す
    if (typeof updateBranchOptions === 'function') {
        updateBranchOptions();
    }
}

function updateInterestRateByLimit(limit) {

    if (limit >= 10 && limit <= 100) {
        interestRateInput.value = 14.5;

    } else if (limit > 100 && limit <= 200) {
        interestRateInput.value = 12.0;

    } else if (limit > 200 && limit <= 300) {
        interestRateInput.value = 10.0;

    } else if (limit > 300 && limit <= 400) {
        interestRateInput.value = 8.0;

    } else if (limit > 400 && limit <= 500) {
        interestRateInput.value = 7.0;

    } else if (limit > 500 && limit <= 600) {
        interestRateInput.value = 6.0;

    } else if (limit > 600 && limit <= 700) {
        interestRateInput.value = 5.0;

    } else if (limit > 700 && limit <= 800) {
        interestRateInput.value = 4.0;

    } else if (limit > 800 && limit <= 1000) {
        interestRateInput.value = 0.95;

    } else {
        interestRateInput.value = '';
        return;
    }

    validateInterestRate();
}

function updateLoanLimit() {

    const limit = calculateLoanLimit();

    loanLimit.textContent =
        '借入限度額：' + limit + '万円';

    updateInterestRateByLimit(limit);
}

annualIncome.addEventListener('input', updateLoanLimit);

updateLoanLimit();
const branchOptions = {
    "山陰共同銀行": [
        "中央支店",
        "北町支店",
        "南町支店"
    ],
    "なないろ銀行": [
        "虹ヶ丘支店",
        "若葉支店",
        "星川支店"
    ],
    "桜中央銀行": [
        "桜町支店",
        "青葉支店",
        "みどり支店"
    ],
    "みなと未来信用銀行": [
        "港中央支店",
        "海岸支店",
        "未来町支店"
    ],
    "つばさ中央銀行": [
        "つばさ支店",
        "東中央支店",
        "西中央支店"
    ]
};

function updateBranchOptions(selectedBranch = '', keyword = '') {

    const selectedBank = bankNameSelect.value;

    // 支店の選択肢をいったん削除
    branchNameSelect.innerHTML = '';

    // 最初の選択肢を作成
    const defaultOption = document.createElement('option');
    defaultOption.value = '';

    if (selectedBank === '') {
        defaultOption.textContent = '金融機関を先に選択してください';
    } else {
        defaultOption.textContent = '選択してください';
    }

    branchNameSelect.appendChild(defaultOption);

    // 選択された金融機関の支店を取得
    const branches = branchOptions[selectedBank];

    if (branches) {

        branches
            .filter(branch => branch.includes(keyword))
            .forEach(branch => {

                const option = document.createElement('option');

                option.value = branch;
                option.textContent = branch;

                branchNameSelect.appendChild(option);
            });
    }

    // 選択していた支店を復元
    if (selectedBranch !== '') {
        branchNameSelect.value = selectedBranch;
    }
}


// 金融機関を変更したとき
bankNameSelect.addEventListener('change', function () {

    branchSearch.value = '';
    selectedBranchInput.value = '';

    updateBranchOptions();
});


// 支店を検索したとき
branchSearch.addEventListener('input', function () {

    // 検索前に現在選択している支店を覚える
    const selectedBranch = branchNameSelect.value;

    selectedBranchInput.value = selectedBranch;
});

// 支店を検索したとき
branchSearch.addEventListener('input', function () {

    const currentBranch = selectedBranchInput.value;

    updateBranchOptions(
        currentBranch,
        this.value
    );
});


// バリデーションエラーなどで画面が再表示されたとき
const savedBranch = selectedBranchInput.value;

updateBranchOptions(savedBranch);

function clearServerError(element) {
    if (!element) {
        return;
    }

    const container =
        element.closest('.name-field') ||
        element.closest('p');

    if (!container) {
        return;
    }

    const serverError =
        container.querySelector('.server-error');

    if (serverError) {
        serverError.textContent = '';
    }
}

function validateLoanAmount() {
    clearServerError(loanAmountInput);

    const value = loanAmountInput.value;

    if (value === '') {
        loanAmountError.textContent = '借入金額を入力してください';

    } else if (!/^[0-9]+$/.test(value)) {
        loanAmountError.textContent = '借入金額は数字で入力してください';

    } else if (Number(value) < 1) {
        loanAmountError.textContent = '借入金額は1万円以上で入力してください';

    } else if (Number(value) > 1000) {
        loanAmountError.textContent = '借入金額は1000万円以下で入力してください';

    } else {
        loanAmountError.textContent = '';
    }
}


function validateBankName() {

    clearServerError(bankNameSelect);

    if (bankNameSelect.value === '') {
        bankNameError.textContent =
            '金融機関名を選択してください';
    } else {
        bankNameError.textContent = '';
    }
}

bankNameSelect.addEventListener('change', validateBankName);
bankNameSelect.addEventListener('blur', validateBankName);


function validateBranchName() {

    clearServerError(branchNameInput);

    const value = branchNameInput.value.trim();

    if (value === '') {
        branchNameError.textContent = '支店名を選択してください';
    } else {
        branchNameError.textContent = '';
    }
}

branchNameInput.addEventListener('input', validateBranchName);
branchNameInput.addEventListener('blur', validateBranchName);

function validateBankAccountType() {

    clearServerError(bankAccountTypeInput);

    const value = bankAccountTypeInput.value.trim();

    if (value === '') {
        bankAccountTypeError.textContent = '科目を選択してください';
    } else {
        bankAccountTypeError.textContent = '';
    }
}

bankAccountTypeInput.addEventListener('input', validateBankAccountType);
bankAccountTypeInput.addEventListener('blur', validateBankAccountType);


function validateBankAccountNum() {

    clearServerError(bankAccountNumInput);

    const value = bankAccountNumInput.value.trim();

    if (value === '') {
        bankAccountNumError.textContent = '口座番号を入力してください';
    } else if (!/^[0-9]+$/.test(value)) {
        bankAccountNumError.textContent = '口座番号は数字で入力してください';
    } else if (value.length !== 7) {
        bankAccountNumError.textContent = '口座番号は7桁で入力してください';
    } else {
        bankAccountNumError.textContent = '';
    }
}

bankAccountNumInput.addEventListener('input', validateBankAccountNum);
bankAccountNumInput.addEventListener('blur', validateBankAccountNum);


function validateAnnualIncome() {

    clearServerError(annualIncomeInput);

    const value = annualIncomeInput.value.trim();

    if (value === '') {
        annualIncomeError.textContent = '年収を入力してください';
    } else if (!/^[0-9]+$/.test(value)) {
        annualIncomeError.textContent = '年収は数字で入力してください';
    } else if (Number(value) < 1) {
        annualIncomeError.textContent = '年収は1万円以上で入力してください';
    } else if (Number(value) > 100000) {
        annualIncomeError.textContent = '年収が上限を超えています';
    } else {
        annualIncomeError.textContent = '';
    }
}


function validateInterestRate() {

    clearServerError(interestRateInput);

    const value = interestRateInput.value.trim();

    if (value === '') {
        interestRateError.textContent = '金利を入力してください';
    } else if (Number.isNaN(Number(value))) {
        interestRateError.textContent = '金利は数字で入力してください';
    } else if (Number(value) < 0.95) {
        interestRateError.textContent = '金利は0.95以上で入力してください';
    } else if (Number(value) > 14.5) {
        interestRateError.textContent = '金利は14.5以下で入力してください';
    } else {
        interestRateError.textContent = '';
    }
}

interestRateInput.addEventListener('input', validateInterestRate);
interestRateInput.addEventListener('blur', validateInterestRate);

function validateLoanLimit() {
    loanLimitError.textContent = '';

    if (loanLimitServerError) {
        loanLimitServerError.textContent = '';
    }

    const loan = Number(loanAmountInput.value);

    if (Number.isNaN(loan)) {
        return;
    }

    const limit = calculateLoanLimit();

    if (loan > limit) {
        loanLimitError.textContent =
            '借入限度額は年収の50％（10万円単位）が上限になります';
    }
}

function validateLastName() {
    clearServerError(lastNameInput);

    if (lastNameInput.value.trim() === '') {
        lastNameError.textContent = '姓を入力してください';
    } else {
        lastNameError.textContent = '';
    }
}

function validateFirstName() {
    clearServerError(firstNameInput);

    if (firstNameInput.value.trim() === '') {
        firstNameError.textContent = '名を入力してください';
    } else {
        firstNameError.textContent = '';
    }
}

function validateLastNameKana() {
    clearServerError(lastNameKanaInput);

    const value = lastNameKanaInput.value.trim();

    if (value === '') {
        lastNameKanaError.textContent =
            '姓のフリガナを入力してください';
    } else if (!/^[ァ-ヶー]+$/.test(value)) {
        lastNameKanaError.textContent =
            '姓のフリガナはカタカナで入力してください';
    } else {
        lastNameKanaError.textContent = '';
    }
}

function validateFirstNameKana() {
    clearServerError(firstNameKanaInput);

    const value = firstNameKanaInput.value.trim();

    if (value === '') {
        firstNameKanaError.textContent =
            '名のフリガナを入力してください';
    } else if (!/^[ァ-ヶー]+$/.test(value)) {
        firstNameKanaError.textContent =
            '名のフリガナはカタカナで入力してください';
    } else {
        firstNameKanaError.textContent = '';
    }
}

function validateBirthDate() {

    // Spring側の古いエラーを消す
    if (birthDateServerError) {
        birthDateServerError.textContent = '';
    }

    if (ageServerError) {
        ageServerError.textContent = '';
    }

    const value = birthDateInput.value;

    if (value === '') {
        birthDateError.textContent =
            '生年月日を入力してください';
        return;
    }

    const birthDate = new Date(value + 'T00:00:00');
    const today = new Date();

    // 未来の日付
    if (birthDate > today) {
        birthDateError.textContent =
            '生年月日は過去の日付を入力してください';
        return;
    }

    let age =
        today.getFullYear() - birthDate.getFullYear();

    const monthDifference =
        today.getMonth() - birthDate.getMonth();

    // 今年の誕生日がまだ来ていなければ1歳引く
    if (
        monthDifference < 0 ||
        (
            monthDifference === 0 &&
            today.getDate() < birthDate.getDate()
        )
    ) {
        age--;
    }

    if (age <= 21) {
        birthDateError.textContent =
            '20歳未満の方はお申し込みいただけません';
    } else {
        birthDateError.textContent = '';
    }
}

function resetTimeout() {

    // 現在時刻から10分後を期限にする
    timeoutDeadline = Date.now() + TIMEOUT_TIME;

    updateTimer();
}

function updateTimer() {

    const remainingTime =
        timeoutDeadline - Date.now();

    // タイムアウト
    if (remainingTime <= 0) {

        clearInterval(countdownInterval);

        sessionTimer.textContent = '00:00';

        alert(
            'セキュリティ保護のため、一定時間操作がなかったため申込内容を破棄しました。'
        );

        window.location.href = '/bankLoan';

        return;
    }

    const totalSeconds =
        Math.ceil(remainingTime / 1000);

    const minutes =
        Math.floor(totalSeconds / 60);

    const seconds =
        totalSeconds % 60;

    sessionTimer.textContent =
        String(minutes).padStart(2, '0')
        + ':'
        + String(seconds).padStart(2, '0');
}

[
    'mousedown',
    'keydown',
    'scroll',
    'touchstart'
].forEach(function (eventName) {

    document.addEventListener(
        eventName,
        resetTimeout
    );

});


resetTimeout();

countdownInterval =
    setInterval(updateTimer, 1000);

lastNameInput.addEventListener('input', validateLastName);
lastNameInput.addEventListener('blur', validateLastName);

firstNameInput.addEventListener('input', validateFirstName);
firstNameInput.addEventListener('blur', validateFirstName);

lastNameKanaInput.addEventListener('input', validateLastNameKana);
lastNameKanaInput.addEventListener('blur', validateLastNameKana);

firstNameKanaInput.addEventListener('input', validateFirstNameKana);
firstNameKanaInput.addEventListener('blur', validateFirstNameKana);

loanAmountInput.addEventListener('input', function () {
    validateLoanAmount();
    validateLoanLimit();
});

annualIncomeInput.addEventListener('input', function () {
    validateAnnualIncome();
    validateLoanLimit();
    updateLoanLimit();
});
loanAmountInput.addEventListener('blur', function () {
    validateLoanAmount();
    validateLoanLimit();
});

annualIncomeInput.addEventListener('blur', function () {
    validateAnnualIncome();
    validateLoanLimit();
});

birthDateInput.addEventListener(
    'input',
    validateBirthDate
);

birthDateInput.addEventListener(
    'blur',
    validateBirthDate
);