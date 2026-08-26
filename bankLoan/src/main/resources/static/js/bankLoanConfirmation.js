const form = document.getElementById('bankLoanConfirmationForm');

form.addEventListener('submit', function(event) {
    const result = confirm('この内容で申し込みますか？');

    if (!result) {
        event.preventDefault();
    }
});