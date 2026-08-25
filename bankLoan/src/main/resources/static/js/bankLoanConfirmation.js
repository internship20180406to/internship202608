const submitButton = document.getElementById("submit");

submitButton.addEventListener("click", (e) => {
    const confirmed = confirm("操作を実行します");

    console.log(confirmed);

    if (!confirmed) {
        e.preventDefault();
    }
});