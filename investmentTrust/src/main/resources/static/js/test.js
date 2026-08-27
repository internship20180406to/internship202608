let maxReachedStep = 1;

const totalSteps = 6;

const stepTitles = {
    1: "金融機関",
    2: "支店",
    3: "科目",
    4: "口座番号",
    5: "購入者",
    6: "注文内容"
};

const prices = {
    "A株式会社": 100,
    "B株式会社": 200,
    "C株式会社": 300,
    "D株式会社": 400
};


function nextStep(currentStep) {

    const currentStepElement =
        document.getElementById("step" + currentStep);

    const nextStepNumber = currentStep + 1;

    const nextStepElement =
        document.getElementById("step" + nextStepNumber);

    if (!nextStepElement) {
        return;
    }

    currentStepElement.style.display = "none";
    nextStepElement.style.display = "block";

    if (nextStepNumber > maxReachedStep) {
        maxReachedStep = nextStepNumber;
    }

    updateProgress(nextStepNumber);
}


function prevStep(currentStep) {

    const currentStepElement =
        document.getElementById("step" + currentStep);

    const previousStepNumber = currentStep - 1;

    const previousStepElement =
        document.getElementById(
            "step" + previousStepNumber
        );

    if (!previousStepElement) {
        return;
    }

    currentStepElement.style.display = "none";
    previousStepElement.style.display = "block";

    updateProgress(previousStepNumber);
}


function updateProgress(step) {

    const progressBar =
        document.getElementById("progressBar");

    const progress =
        ((step - 1) / (totalSteps - 1)) * 100;

    progressBar.style.width = progress + "%";

    const currentStepNumber =
        document.getElementById("currentStepNumber");

    if (currentStepNumber) {
        currentStepNumber.textContent =
            "STEP " + step + " / " + totalSteps;
    }

    const currentStepTitle =
        document.getElementById("currentStepTitle");

    if (currentStepTitle) {
        currentStepTitle.textContent =
            stepTitles[step];
    }

    for (let i = 1; i <= totalSteps; i++) {

        const circle =
            document.getElementById(
                "stepCircle" + i
            );

        if (!circle) {
            continue;
        }

        circle.classList.toggle(
            "completed",
            i <= step
        );

        circle.classList.toggle(
            "active",
            i === step
        );

        circle.disabled = i >= step;
    }
}


function goToStep(targetStep) {

    if (
        targetStep < 1 ||
        targetStep > totalSteps ||
        targetStep > maxReachedStep
    ) {
        return;
    }

    for (let i = 1; i <= totalSteps; i++) {

        const step =
            document.getElementById("step" + i);

        if (step) {
            step.style.display = "none";
        }
    }

    const target =
        document.getElementById("step" + targetStep);

    if (target) {
        target.style.display = "block";
    }

    updateProgress(targetStep);
}


function validateBankAccountNum() {

    const bankAccountNum =
        document.getElementById(
            "bankAccountNum"
        ).value;

    const error =
        document.getElementById(
            "bankAccountNumError"
        );

    error.style.display = "none";
    error.textContent = "";

    if (bankAccountNum === "") {

        error.textContent =
            "口座番号が入力されていません。";

        error.style.display = "block";

        return;
    }

    if (!/^\d+$/.test(bankAccountNum)) {

        error.textContent =
            "口座番号は半角数字で入力してください。";

        error.style.display = "block";

        return;
    }

    if (bankAccountNum.length !== 7) {

        error.textContent =
            "口座番号は7桁で入力してください。";

        error.style.display = "block";

        return;
    }

    nextStep(4);
}


function validateName() {

    const nameInput =
        document.getElementById("name");

    const nameError =
        document.getElementById("nameError");

    let name =
        nameInput.value.trim();

    if (name === "") {

        nameError.textContent =
            "購入者名を入力してください。";

        nameError.style.display = "block";

        return;
    }

    name =
        convertHiraganaToHalfWidthKatakana(name);

    nameInput.value = name;

    const halfWidthKatakana =
        /^[ｦ-ﾟ]+$/;

    if (!halfWidthKatakana.test(name)) {

        nameError.textContent =
            "ひらがなで入力してください。";

        nameError.style.display = "block";

        return;
    }

    nameError.style.display = "none";

    nextStep(5);
}


function updatePrice() {

    const fundName =
        document.getElementById("fundName").value;

    const unitPrice =
        document.getElementById("unitPrice");

    if (fundName === "") {
        unitPrice.value = "";
        return;
    }

    unitPrice.value =
        prices[fundName] + "円";
}


function getUnitPrice() {

    const fundName =
        document.getElementById("fundName").value;

    return prices[fundName] || 0;
}


function calculateQuantity() {

    const money =
        document.getElementById("money").value;

    const unitPrice =
        getUnitPrice();

    const quantity =
        document.getElementById("quantity");

    if (
        money === "" ||
        unitPrice === 0
    ) {
        quantity.value = "";
        return;
    }

    quantity.value =
        Math.floor(
            Number(money) / unitPrice
        );
}


function validateInvestment() {

    const fundName =
        document.getElementById("fundName").value;

    const money =
        document.getElementById("money").value;

    const moneyError =
        document.getElementById("moneyError");

    const quantityError =
        document.getElementById("quantityError");

    moneyError.style.display = "none";
    moneyError.textContent = "";

    quantityError.style.display = "none";
    quantityError.textContent = "";

    if (fundName === "") {
        return;
    }

    if (money === "") {

        moneyError.textContent =
            "購入金額を入力してください。";

        moneyError.style.display = "block";

        return;
    }

    const unitPrice =
        getUnitPrice();

    const calculatedQuantity =
        Math.floor(
            Number(money) / unitPrice
        );

    if (calculatedQuantity <= 0) {

        moneyError.textContent =
            "購入金額が1口の価格を下回っています。";

        moneyError.style.display = "block";

        return;
    }

    document.getElementById("quantity").value =
        calculatedQuantity;

    document.getElementById("money").value =
        calculatedQuantity * unitPrice;

    document.querySelector("form").submit();
}


function convertHiraganaToHalfWidthKatakana(value) {

    value = value.replace(
        /[ぁ-ゖ]/g,
        function(char) {
            return String.fromCharCode(
                char.charCodeAt(0) + 0x60
            );
        }
    );

    const kanaMap = {
        "ア": "ｱ",
        "イ": "ｲ",
        "ウ": "ｳ",
        "エ": "ｴ",
        "オ": "ｵ",
        "カ": "ｶ",
        "キ": "ｷ",
        "ク": "ｸ",
        "ケ": "ｹ",
        "コ": "ｺ",
        "サ": "ｻ",
        "シ": "ｼ",
        "ス": "ｽ",
        "セ": "ｾ",
        "ソ": "ｿ",
        "タ": "ﾀ",
        "チ": "ﾁ",
        "ツ": "ﾂ",
        "テ": "ﾃ",
        "ト": "ﾄ",
        "ナ": "ﾅ",
        "ニ": "ﾆ",
        "ヌ": "ﾇ",
        "ネ": "ﾈ",
        "ノ": "ﾉ",
        "ハ": "ﾊ",
        "ヒ": "ﾋ",
        "フ": "ﾌ",
        "ヘ": "ﾍ",
        "ホ": "ﾎ",
        "マ": "ﾏ",
        "ミ": "ﾐ",
        "ム": "ﾑ",
        "メ": "ﾒ",
        "モ": "ﾓ",
        "ヤ": "ﾔ",
        "ユ": "ﾕ",
        "ヨ": "ﾖ",
        "ラ": "ﾗ",
        "リ": "ﾘ",
        "ル": "ﾙ",
        "レ": "ﾚ",
        "ロ": "ﾛ",
        "ワ": "ﾜ",
        "ヲ": "ｦ",
        "ン": "ﾝ",
        "ァ": "ｧ",
        "ィ": "ｨ",
        "ゥ": "ｩ",
        "ェ": "ｪ",
        "ォ": "ｫ",
        "ッ": "ｯ",
        "ャ": "ｬ",
        "ュ": "ｭ",
        "ョ": "ｮ",
        "ー": "ｰ",
        "「": "｢",
        "」": "｣",
        "、": "､",
        "。": "｡"
    };

    return value.replace(
        /[アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンァィゥェォッャュョー「」、。]/g,
        function(char) {
            return kanaMap[char] || char;
        }
    );
}