let maxReachedStep = 1;

function nextStep(currentStep) {

    document.getElementById("step" + currentStep).style.display = "none";

    const nextStepNumber = currentStep + 1;

    const nextStepElement =
        document.getElementById("step" + nextStepNumber);

    if (nextStepElement) {

        nextStepElement.style.display = "block";

        updateProgress(nextStepNumber);

    }
}


function prevStep(currentStep) {

    document.getElementById("step" + currentStep).style.display = "none";

    const previousStepNumber = currentStep - 1;

    const previousStepElement =
        document.getElementById("step" + previousStepNumber);

    if (previousStepElement) {

        previousStepElement.style.display = "block";

        updateProgress(previousStepNumber);

    }
}

function updateProgress(step) {

    const progressBar =
        document.getElementById("progressBar");

    const totalSteps = 6;

    /*
     * STEPごとの進捗率
     *
     * STEP1 = 0%
     * STEP2 = 20%
     * STEP3 = 40%
     * STEP4 = 60%
     * STEP5 = 80%
     * STEP6 = 100%
     */
    const progress =
        ((step - 1) / (totalSteps - 1)) * 100;

    progressBar.style.width = progress + "%";


    /*
     * 現在のSTEP表示を更新
     */

    const currentStepNumber =
        document.getElementById("currentStepNumber");

    if (currentStepNumber) {

        currentStepNumber.textContent =
            "STEP " + step + " / " + totalSteps;

    }


    /*
     * 現在のSTEPタイトルを更新
     */

    const currentStepTitle =
        document.getElementById("currentStepTitle");

    const stepTitles = {

        1: "金融機関",

        2: "支店",

        3: "科目",

        4: "口座番号",

        5: "購入者",

        6: "注文内容"

    };

    if (currentStepTitle) {

        currentStepTitle.textContent =
            stepTitles[step];

    }


    /*
     * ステップの○を更新
     */

    for (let i = 1; i <= totalSteps; i++) {

        const circle =
            document.getElementById("stepCircle" + i);

        if (!circle) {
            continue;
        }


        /*
         * 現在のSTEPまで緑色にする
         */

        if (i <= step) {

            circle.classList.add("completed");

        } else {

            circle.classList.remove("completed");

        }


        /*
         * 現在のSTEPをactiveにする
         */

        if (i === step) {

            circle.classList.add("active");

        } else {

            circle.classList.remove("active");

        }


        /*
         * 現在のSTEPより前のSTEPはクリック可能
         */

        if (i < step) {

            circle.disabled = false;

        } else if (i > step) {

            circle.disabled = true;

        }

    }
}

function goToStep(targetStep) {

    /*
     * 存在しないSTEPへの移動を防止
     */

    if (targetStep < 1 || targetStep > 6) {
        return;
    }


    /*
     * まだ到達していないSTEPには
     * 移動できない
     */

    if (targetStep > maxReachedStep) {
        return;
    }


    /*
     * 全STEPを非表示
     */

    for (let i = 1; i <= 6; i++) {

        const step =
            document.getElementById(
                "step" + i
            );


        if (step) {

            step.style.display = "none";

        }
    }


    /*
     * 移動先を表示
     */

    const target =
        document.getElementById(
            "step" + targetStep
        );


    if (target) {

        target.style.display = "block";

    }


    /*
     * 進捗表示を更新
     */

    updateProgress(targetStep);
}

function validateBankAccountNum() {

    const bankAccountNum =
        document.getElementById("bankAccountNum").value;

    const error =
        document.getElementById("bankAccountNumError");

    error.style.display = "none";
    error.textContent = "";

    if (bankAccountNum === "") {

        error.textContent =
            "口座番号が入力されていません。";

        error.style.display = "block";

        return;
    }

    if (!/^\d{7}$/.test(bankAccountNum)) {

        error.textContent =
            "口座番号は7桁で入力してください。";

        error.style.display = "block";

        return;
    }

    nextStep(4);
}


function validateName() {

    const name =
        document.getElementById("name").value;

    const error =
        document.getElementById("nameError");

    error.style.display = "none";
    error.textContent = "";

    if (name.trim() === "") {

        error.textContent =
            "購入者名が入力されていません。";

        error.style.display = "block";

        return;
    }

    nextStep(5);
}


function updatePrice() {

    const fundName =
        document.getElementById("fundName").value;

    const unitPrice =
        document.getElementById("unitPrice");

    const prices = {

        "A株式会社": 100,
        "B株式会社": 200,
        "C株式会社": 300,
        "D株式会社": 400

    };

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

    const prices = {

        "A株式会社": 100,
        "B株式会社": 200,
        "C株式会社": 300,
        "D株式会社": 400

    };

    return prices[fundName] || 0;
}

function calculateQuantity() {

    const money =
        document.getElementById("money").value;

    const unitPrice =
        getUnitPrice();

    const quantity =
        document.getElementById("quantity");

    if (money === "" || unitPrice === 0) {

        quantity.value = "";

        return;
    }

    /*
     * 購入金額から購入可能な口数を計算
     *
     * 例：
     * 2100円 ÷ 200円
     * → 10口
     */
    const calculatedQuantity =
        Math.floor(Number(money) / unitPrice);

    quantity.value =
        calculatedQuantity;
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


    /*
     * 銘柄未選択
     */
    if (fundName === "") {

        return;
    }


    /*
     * 購入金額未入力
     */
    if (money === "") {

        moneyError.textContent =
            "購入金額を入力してください。";

        moneyError.style.display = "block";

        return;
    }


    const unitPrice =
        getUnitPrice();


    /*
     * 購入金額から口数を計算
     */
    const calculatedQuantity =
        Math.floor(Number(money) / unitPrice);


    /*
     * 1口も購入できない場合
     */
    if (calculatedQuantity <= 0) {

        moneyError.textContent =
            "購入金額が1口の価格を下回っています。";

        moneyError.style.display = "block";

        return;
    }


    /*
     * 口数を設定
     */
    document.getElementById("quantity").value =
        calculatedQuantity;


    /*
     * 実際の購入金額を計算
     *
     * 例：
     * 2100円
     * ↓
     * 10口
     * ↓
     * 10 × 200円
     * ↓
     * 2000円
     */
    const actualMoney =
        calculatedQuantity * unitPrice;


    document.getElementById("money").value =
        actualMoney;


    /*
     * 確認画面へ送信
     */
    document.querySelector("form").submit();
}

function goBackToInput(step) {

    window.location.href =
        "/investmentTrust?step=" + step;
}

