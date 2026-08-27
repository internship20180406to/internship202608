const form =
    document.getElementById('bankLoanConfirmationForm');

form.addEventListener('submit', async function(event) {

    event.preventDefault();

    const response =
        await fetch('/bankLoan/sessionActivity', {
            method: 'POST'
        });

    const data =
        await response.json();

    if (data.expired) {
        window.location.href =
            '/bankLoanTimeout';
        return;
    }

    const result =
        confirm('この内容で申し込みますか？');

    if (result) {
        form.submit();
    }
});

const bankAccountNumConfirm =
    document.getElementById('bankAccountNumConfirm');

const toggleAccountNumberConfirm =
    document.getElementById('toggleAccountNumberConfirm');

toggleAccountNumberConfirm.addEventListener(
    'click',
    function () {

        if (bankAccountNumConfirm.type === 'password') {

            bankAccountNumConfirm.type = 'text';
            this.textContent = '隠す';

        } else {

            bankAccountNumConfirm.type = 'password';
            this.textContent = '表示';
        }
    }
);