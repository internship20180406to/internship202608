const validationForms =
    document.querySelectorAll(".needs-validation");

validationForms.forEach((form) => {
    form.addEventListener(
        "invalid",
        () => {
            form.classList.add("was-validated");
        },
        true
    );

    form.addEventListener("reset", () => {
        form.classList.remove("was-validated");
    });
});