function nextStep(currentStep) {

    document.getElementById("step" + currentStep).style.display = "none";

    document.getElementById("step" + (currentStep + 1)).style.display = "block";

    updateProgress(currentStep);
}


function prevStep(currentStep) {

    document.getElementById("step" + currentStep).style.display = "none";

    document.getElementById("step" + (currentStep - 1)).style.display = "block";

    updateProgress(currentStep - 2);
}


function updateProgress(step) {

    const progressBar =
        document.getElementById("progressBar");

    const progress = (step / 7) * 100;

    progressBar.style.width = progress + "%";
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


function calculateMoney() {

    const quantity =
        document.getElementById("quantity").value;

    const unitPrice =
        getUnitPrice();

    const money =
        document.getElementById("money");

    if (quantity === "" || unitPrice === 0) {

        return;
    }

    money.value =
        quantity * unitPrice;
}


function calculateQuantity() {

    const money =
        document.getElementById("money").value;

    const unitPrice =
        getUnitPrice();

    const quantity =
        document.getElementById("quantity");

    if (money === "" || unitPrice === 0) {

        return;
    }

    quantity.value =
        Math.floor(money / unitPrice);
}


function validateInvestment() {

    const fundName =
        document.getElementById("fundName").value;

    const quantity =
        document.getElementById("quantity").value;

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

    if (quantity === "" && money === "") {

        quantityError.textContent =
            "口数または購入金額を入力してください。";

        quantityError.style.display = "block";

        return;
    }

    if (quantity === "") {

        calculateQuantity();

    } else if (money === "") {

        calculateMoney();
    }

    const finalQuantity =
        document.getElementById("quantity").value;

    const finalUnitPrice =
        getUnitPrice();

    const finalMoney =
        document.getElementById("money");

    finalMoney.value =
        finalQuantity * finalUnitPrice;

    document.querySelector("form").submit();
}