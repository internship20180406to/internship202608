const submitButton = document.getElementById("submit")
    submitButton.addEventListener('click', (e) => {
        const isConfirmed = confirm("この内容で振込を確定します");
        console.log(isConfirmed);
        if (!isConfirmed) {
            e.preventDefault();
        }
    })