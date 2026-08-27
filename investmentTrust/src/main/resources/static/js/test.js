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


/* =========================
   初期表示
   ========================= */

document.addEventListener("DOMContentLoaded", function () {

    const startStep =
        typeof initialStep !== "undefined"
            ? initialStep
            : 1;

    if (typeof editMode !== "undefined" && editMode) {
        maxReachedStep = startStep;
    }

    goToStep(startStep);

    setupFundSearch();
});


/* =========================
   STEP移動
   ========================= */

function nextStep(currentStep) {

    /* STEP1 金融機関チェック */
    if (currentStep === 1) {

        const bankName =
            document.getElementById("bankName");

        if (!bankName || bankName.value === "") {

            alert("金融機関名を選択してください。");

            return;
        }
    }


    /* STEP2 支店チェック */
    if (currentStep === 2) {

        const branchName =
            document.getElementById("branchName");

        if (!branchName || branchName.value === "") {

            alert("支店名を選択してください。");

            return;
        }
    }


    /* STEP3 科目チェック */
    if (currentStep === 3) {

        const bankAccountTypeName =
            document.getElementById(
                "bankAccountTypeName"
            );

        if (
            !bankAccountTypeName ||
            bankAccountTypeName.value === ""
        ) {

            alert("科目名を選択してください。");

            return;
        }
    }


    const currentStepElement =
        document.getElementById(
            "step" + currentStep
        );

    const nextStepNumber =
        currentStep + 1;

    const nextStepElement =
        document.getElementById(
            "step" + nextStepNumber
        );

    if (!nextStepElement) {
        return;
    }

    currentStepElement.style.display =
        "none";

    nextStepElement.style.display =
        "block";


    if (nextStepNumber > maxReachedStep) {

        maxReachedStep =
            nextStepNumber;
    }

    updateProgress(nextStepNumber);
}


function prevStep(currentStep) {

    const currentStepElement =
        document.getElementById(
            "step" + currentStep
        );

    const previousStepNumber =
        currentStep - 1;

    const previousStepElement =
        document.getElementById(
            "step" + previousStepNumber
        );

    if (!previousStepElement) {
        return;
    }

    currentStepElement.style.display =
        "none";

    previousStepElement.style.display =
        "block";

    updateProgress(previousStepNumber);
}


function updateProgress(step) {

    const progressBar =
        document.getElementById(
            "progressBar"
        );

    const progress =
        ((step - 1) /
            (totalSteps - 1)) *
        100;

    if (progressBar) {

        progressBar.style.width =
            progress + "%";
    }


    const currentStepNumber =
        document.getElementById(
            "currentStepNumber"
        );

    if (currentStepNumber) {

        currentStepNumber.textContent =
            "STEP " +
            step +
            " / " +
            totalSteps;
    }


    const currentStepTitle =
        document.getElementById(
            "currentStepTitle"
        );

    if (currentStepTitle) {

        currentStepTitle.textContent =
            stepTitles[step];
    }


    for (
        let i = 1;
        i <= totalSteps;
        i++
    ) {

        const circle =
            document.getElementById(
                "stepCircle" + i
            );

        if (!circle) {
            continue;
        }

        circle.classList.toggle(
            "completed",
            i < step
        );

        circle.classList.toggle(
            "active",
            i === step
        );

        /*
         * 今いるSTEPは押せない。
         * それ以前の到達済みSTEPは押せる。
         */
        circle.disabled =
            i > maxReachedStep ||
            i === step;
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


    for (
        let i = 1;
        i <= totalSteps;
        i++
    ) {

        const step =
            document.getElementById(
                "step" + i
            );

        if (step) {

            step.style.display =
                "none";
        }
    }


    const target =
        document.getElementById(
            "step" + targetStep
        );

    if (target) {

        target.style.display =
            "block";
    }

    updateProgress(targetStep);
}


/* =========================
   STEP4 口座番号
   ========================= */

function validateBankAccountNum() {

    const bankAccountNum =
        document.getElementById(
            "bankAccountNum"
        ).value;

    const error =
        document.getElementById(
            "bankAccountNumError"
        );

    error.style.display =
        "none";

    error.textContent =
        "";


    if (bankAccountNum === "") {

        error.textContent =
            "口座番号が入力されていません。";

        error.style.display =
            "block";

        return;
    }


    if (!/^\d+$/.test(bankAccountNum)) {

        error.textContent =
            "口座番号は半角数字で入力してください。";

        error.style.display =
            "block";

        return;
    }


    if (bankAccountNum.length !== 7) {

        error.textContent =
            "口座番号は7桁で入力してください。";

        error.style.display =
            "block";

        return;
    }

    nextStep(4);
}


/* =========================
   STEP5 購入者名
   ========================= */

function validateName() {

    const nameInput =
        document.getElementById("name");

    const nameError =
        document.getElementById("nameError");

    let name =
        nameInput.value.trim();


    nameError.style.display =
        "none";

    nameError.textContent =
        "";


    if (name === "") {

        nameError.textContent =
            "購入者名を入力してください。";

        nameError.style.display =
            "block";

        return;
    }


    name =
        convertHiraganaToHalfWidthKatakana(
            name
        );

    nameInput.value =
        name;


    const halfWidthKatakana =
        /^[ｦ-ﾟ]+$/;


    if (!halfWidthKatakana.test(name)) {

        nameError.textContent =
            "ひらがなで入力してください。";

        nameError.style.display =
            "block";

        return;
    }

    nextStep(5);
}


/* =========================
   銘柄検索
   ========================= */

function setupFundSearch() {

    const fundSearch =
        document.getElementById(
            "fundSearch"
        );

    const fundSearchResults =
        document.getElementById(
            "fundSearchResults"
        );


    if (!fundSearch ||
        !fundSearchResults) {

        return;
    }


    let fundSearchTimer =
        null;


    fundSearch.addEventListener(
        "input",
        function () {

            const keyword =
                fundSearch.value.trim();


            clearTimeout(
                fundSearchTimer
            );


            /*
             * 入力を変更したら、
             * 以前選択した銘柄を解除
             */
            const fundName =
                document.getElementById(
                    "fundName"
                );

            const selectedFundName =
                document.getElementById(
                    "selectedFundName"
                );

            const unitPrice =
                document.getElementById(
                    "unitPrice"
                );


            if (fundName) {
                fundName.value = "";
            }

            if (selectedFundName) {

                selectedFundName.textContent =
                    "";

                selectedFundName.style.display =
                    "none";
            }

            if (unitPrice) {
                unitPrice.value = "";
            }


            /*
             * 手数料表示もリセット
             */
            const feeDisplay =
                document.getElementById(
                    "feeDisplay"
                );

            const totalMoneyDisplay =
                document.getElementById(
                    "totalMoneyDisplay"
                );

            const totalMoney =
                document.getElementById(
                    "totalMoney"
                );

            if (feeDisplay) {
                feeDisplay.textContent = "";
            }

            if (totalMoneyDisplay) {
                totalMoneyDisplay.textContent = "";
            }

            if (totalMoney) {
                totalMoney.value = "";
            }


            if (keyword === "") {

                fundSearchResults.innerHTML =
                    "";

                fundSearchResults.style.display =
                    "none";

                return;
            }


            /*
             * 300ms待ってから検索
             */
            fundSearchTimer =
                setTimeout(
                    function () {

                        searchFunds(
                            keyword
                        );

                    },
                    300
                );
        }
    );
}


/**
 * DBから銘柄を検索
 */
function searchFunds(keyword) {

    const fundSearchResults =
        document.getElementById(
            "fundSearchResults"
        );


    if (!fundSearchResults) {
        return;
    }


    fetch(
        "/api/funds?keyword=" +
        encodeURIComponent(keyword)
    )
        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "銘柄検索に失敗しました"
                );
            }

            return response.json();
        })
        .then(function (funds) {

            displayFundResults(
                funds
            );
        })
        .catch(function (error) {

            console.error(error);

            fundSearchResults.innerHTML =
                "<div style='padding:10px;color:red;'>" +
                "検索に失敗しました。" +
                "</div>";

            fundSearchResults.style.display =
                "block";
        });
}


/**
 * 検索結果を表示
 */
function displayFundResults(funds) {

    const fundSearchResults =
        document.getElementById(
            "fundSearchResults"
        );


    if (!fundSearchResults) {
        return;
    }


    fundSearchResults.innerHTML =
        "";


    if (!funds ||
        funds.length === 0) {

        fundSearchResults.innerHTML =
            "<div style='padding:10px;color:#777;'>" +
            "該当する銘柄がありません。" +
            "</div>";

        fundSearchResults.style.display =
            "block";

        return;
    }


    funds.forEach(function (fund) {

        const item =
            document.createElement(
                "div"
            );


        item.textContent =
            fund.fundName +
            "　（1口 " +
            Number(
                fund.unitPrice
            ).toLocaleString() +
            "円）";


        item.style.padding =
            "12px";

        item.style.cursor =
            "pointer";

        item.style.borderBottom =
            "1px solid #eeeeee";


        item.addEventListener(
            "mouseenter",
            function () {

                item.style.backgroundColor =
                    "#f5f5f5";
            }
        );


        item.addEventListener(
            "mouseleave",
            function () {

                item.style.backgroundColor =
                    "white";
            }
        );


        item.addEventListener(
            "click",
            function () {

                selectFund(fund);
            }
        );


        fundSearchResults.appendChild(
            item
        );
    });


    fundSearchResults.style.display =
        "block";
}


/**
 * 銘柄を選択
 */
function selectFund(fund) {

    const fundSearch =
        document.getElementById(
            "fundSearch"
        );

    const fundSearchResults =
        document.getElementById(
            "fundSearchResults"
        );

    const fundName =
        document.getElementById(
            "fundName"
        );

    const selectedFundName =
        document.getElementById(
            "selectedFundName"
        );

    const unitPrice =
        document.getElementById(
            "unitPrice"
        );


    /*
     * 検索欄に選択した銘柄名を表示
     */
    fundSearch.value =
        fund.fundName;


    /*
     * Spring Bootへ送信される銘柄名
     */
    fundName.value =
        fund.fundName;


    /*
     * 選択中の銘柄を表示
     */
    selectedFundName.textContent =
        "選択中: " +
        fund.fundName;

    selectedFundName.style.display =
        "block";


    /*
     * 1口価格
     */
    unitPrice.value =
        Number(
            fund.unitPrice
        ).toLocaleString() +
        "円";


    /*
     * 手数料率を保存
     *
     * fund_tableのfeeRateを使用
     *
     * 例：
     * A株式会社 → 1.10
     * B株式会社 → 3.00
     * C株式会社 → 2.20
     * D株式会社 → 2.20
     */
    const feeRate =
        fund.feeRate != null
            ? Number(fund.feeRate)
            : 0;


    /*
     * 選択した銘柄に
     * 手数料率を保存
     */
    fundName.dataset.feeRate =
        feeRate;


    /*
     * 手数料率表示
     */
    const feeRateDisplay =
        document.getElementById(
            "feeRateDisplay"
        );

    if (feeRateDisplay) {

        feeRateDisplay.textContent =
            "手数料率：" +
            feeRate.toFixed(2) +
            "%";
    }


    /*
     * 検索結果を閉じる
     */
    fundSearchResults.innerHTML =
        "";

    fundSearchResults.style.display =
        "none";


    /*
     * 口数・手数料を計算
     */
    calculateQuantity();
}


/* =========================
   購入金額・手数料・口数
   ========================= */

/**
 * 1口の価格を取得
 */
function getUnitPrice() {

    const unitPriceInput =
        document.getElementById(
            "unitPrice"
        );


    if (!unitPriceInput ||
        unitPriceInput.value === "") {

        return 0;
    }


    return Number(
        unitPriceInput.value
            .replace(/,/g, "")
            .replace("円", "")
    );
}


/**
 * 選択中の銘柄の手数料率を取得
 */
function getFeeRate() {

    const fundName =
        document.getElementById(
            "fundName"
        );


    if (!fundName) {
        return 0;
    }


    const feeRate =
        fundName.dataset.feeRate;


    if (
        feeRate === undefined ||
        feeRate === ""
    ) {

        return 0;
    }


    return Number(feeRate);
}


/**
 * 購入金額から手数料・合計金額・口数を計算
 */
function calculateQuantity() {

    const moneyInput =
        document.getElementById(
            "money"
        );

    const quantity =
        document.getElementById(
            "quantity"
        );

    const totalMoney =
        document.getElementById(
            "totalMoney"
        );


    if (!moneyInput ||
        !quantity) {

        return;
    }


    /*
     * 商品代金
     */
    const money =
        Number(moneyInput.value);


    /*
     * 1口価格
     */
    const unitPrice =
        getUnitPrice();


    /*
     * 手数料率
     *
     * 選択した銘柄のfeeRate
     */
    const feeRate =
        getFeeRate();


    /*
     * 手数料
     *
     * 例：
     * 商品代金10000円
     * 手数料率1.10%
     *
     * 10000 × 0.011
     * = 110円
     */
    const fee =
        Math.floor(
            money *
            (feeRate / 100)
        );


    /*
     * 手数料込みの金額
     */
    const totalMoneyValue =
        money + fee;


    /*
     * 手数料表示
     */
    const feeDisplay =
        document.getElementById(
            "feeDisplay"
        );


    if (feeDisplay) {

        feeDisplay.textContent =
            "内手数料：" +
            fee.toLocaleString() +
            "円";
    }


    /*
     * 手数料込み金額表示
     */
    const totalMoneyDisplay =
        document.getElementById(
            "totalMoneyDisplay"
        );


    if (totalMoneyDisplay) {

        totalMoneyDisplay.textContent =
            "手数料込み：" +
            totalMoneyValue.toLocaleString() +
            "円";
    }


    /*
     * Spring Bootへ送信する金額
     */
    if (totalMoney) {

        totalMoney.value =
            totalMoneyValue;
    }


    /*
     * 購入金額または1口価格が
     * 未入力の場合
     */
    if (
        moneyInput.value === "" ||
        unitPrice === 0
    ) {

        quantity.value =
            "";

        return;
    }


    /*
     * 口数を計算
     *
     * 商品代金 ÷ 1口価格
     */
    quantity.value =
        Math.floor(
            money /
            unitPrice
        );
}


/* =========================
   注文確認
   ========================= */

function validateInvestment() {

    const fundName =
        document.getElementById(
            "fundName"
        ).value;

    const moneyInput =
        document.getElementById(
            "money"
        );

    const money =
        moneyInput.value;


    const moneyError =
        document.getElementById(
            "moneyError"
        );

    const quantityError =
        document.getElementById(
            "quantityError"
        );


    moneyError.style.display =
        "none";

    moneyError.textContent =
        "";

    quantityError.style.display =
        "none";

    quantityError.textContent =
        "";


    /*
     * 銘柄未選択
     */
    if (fundName === "") {

        moneyError.textContent =
            "銘柄を検索して選択してください。";

        moneyError.style.display =
            "block";

        return;
    }


    /*
     * 購入金額未入力
     */
    if (money === "") {

        moneyError.textContent =
            "購入金額を入力してください。";

        moneyError.style.display =
            "block";

        return;
    }


    const unitPrice =
        getUnitPrice();


    if (unitPrice <= 0) {

        moneyError.textContent =
            "銘柄を正しく選択してください。";

        moneyError.style.display =
            "block";

        return;
    }


    /*
     * 商品代金を基準に口数を計算
     */
    const calculatedQuantity =
        Math.floor(
            Number(money) /
            unitPrice
        );


    if (calculatedQuantity <= 0) {

        moneyError.textContent =
            "購入金額が1口の価格を下回っています。";

        moneyError.style.display =
            "block";

        return;
    }


    /*
     * 実際の商品代金
     *
     * 口数 × 1口価格
     */
    const actualMoney =
        calculatedQuantity *
        unitPrice;


    /*
     * 選択した銘柄の手数料率
     */
    const feeRate =
        getFeeRate();


    /*
     * 手数料
     */
    const fee =
        Math.floor(
            actualMoney *
            (feeRate / 100)
        );


    /*
     * 手数料込みの金額
     */
    const totalMoneyValue =
        actualMoney + fee;


    /*
     * 口数
     */
    document.getElementById(
        "quantity"
    ).value =
        calculatedQuantity;


    /*
     * Spring Bootへ送信する金額
     *
     * 商品代金 + 手数料
     */
    document.getElementById(
        "totalMoney"
    ).value =
        totalMoneyValue;


    /*
     * 最終的な手数料表示
     */
    const feeDisplay =
        document.getElementById(
            "feeDisplay"
        );

    if (feeDisplay) {

        feeDisplay.textContent =
            "内手数料：" +
            fee.toLocaleString() +
            "円";
    }


    /*
     * 最終的な手数料込み金額表示
     */
    const totalMoneyDisplay =
        document.getElementById(
            "totalMoneyDisplay"
        );

    if (totalMoneyDisplay) {

        totalMoneyDisplay.textContent =
            "手数料込み：" +
            totalMoneyValue.toLocaleString() +
            "円";
    }


    /*
     * 確認画面へ送信
     */
    document.querySelector(
        "form"
    ).submit();
}


/* =========================
   ひらがな → 半角カタカナ
   ========================= */

function convertHiraganaToHalfWidthKatakana(
    value
) {

    value =
        value.replace(
            /[ぁ-ゖ]/g,
            function(char) {

                return String.fromCharCode(
                    char.charCodeAt(0) +
                    0x60
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

            return kanaMap[char] ||
                char;
        }
    );
}