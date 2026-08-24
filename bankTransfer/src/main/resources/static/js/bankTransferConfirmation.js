const submitButton = document.getElementById("submit")
    submitButton.addEventListener('click', (e) => {
        const isConfirmed = confirm("操作を実行します");
        console.log(isConfirmed);
        if (!isConfirmed) {
            e.preventDefault();
        }
    })