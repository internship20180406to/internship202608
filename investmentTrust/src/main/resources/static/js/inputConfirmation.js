const confirmButton = document.getElementById("confirm")
const inputName = document.getElementById("name");
const inputAccountNum = document.getElementById("number");
const inputMoney = document.getElementById("money");
const name_error = document.getElementById("name_error");
const accountnum_error = document.getElementById("accountnum_error");
const money_error = document.getElementById("money_error");

    confirmButton.addEventListener('click', (e) => {
        if (inputName.value === "") {
            name_error.removeAttribute('hidden');
            e.preventDefault();
        } else {
            name_error.setAttribute('hidden', '');      //  setAribute:要素の編集
        }
        if (inputAccountNum.value === "") {
            accountnum_error.removeAttribute('hidden');
            e.preventDefault();
        } else {
            accountnum_error.setAttribute('hidden', '');
        }
        if (inputMoney.value === "") {
            money_error.removeAttribute('hidden');
            e.preventDefault();
        } else {
            money_error.setAttribute('hidden', '');
        }
})