const submitButton = document.getElementById("submit");

if (submitButton) {
    submitButton.addEventListener('click', (e) => {
        console.log(confirm("操作を実行します"));
    });
}