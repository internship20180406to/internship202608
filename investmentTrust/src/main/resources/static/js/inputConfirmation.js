const confirmButton = document.getElementById("confirm")
const inputName = document.getElementById("name");
const inputAccountNum = document.getElementById("number");
const inputMoney = document.getElementById("money");
const name_error = document.getElementById("name_error");
const accountnum_error_null = document.getElementById("accountnum_error_null");
const accountnum_error_digit = document.getElementById("accountnum_error_digit");
const money_error = document.getElementById("money_error");

    confirmButton.addEventListener('click', (e) => {
        if (inputName.value === "") {
            name_error.removeAttribute('hidden');
            e.preventDefault();
        } else {
            name_error.setAttribute('hidden', '');      //  setAribute:要素の編集
        }
        if (inputAccountNum.value === "") {
            accountnum_error_null.removeAttribute('hidden');
            accountnum_error_digit.setAttribute('hidden', '');
            e.preventDefault();
        } else if (String(inputAccountNum.value).length !== 7 || inputAccountNum.value < 0) {
            accountnum_error_null.setAttribute('hidden', '');
            accountnum_error_digit.removeAttribute('hidden');
            e.preventDefault();
        }
        else {
            accountnum_error_null.setAttribute('hidden', '');
            accountnum_error_digit.setAttribute('hidden', '');
        }
        if (inputMoney.value === "") {
            money_error.removeAttribute('hidden');
            e.preventDefault();
        } else {
            money_error.setAttribute('hidden', '');
        }
})