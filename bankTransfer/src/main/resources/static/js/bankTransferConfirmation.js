const submitButton = document.getElementById("transferSubmitButton");
const requiresReconfirmation = submitButton.dataset.requiresReconfirmation === "true";
const form = submitButton.closest("form");

const overlay = document.getElementById("reconfirmModalOverlay");
const check1 = document.getElementById("reconfirmCheck1");
const check2 = document.getElementById("reconfirmCheck2");
const backButton = document.getElementById("reconfirmBackButton");
const confirmButton = document.getElementById("reconfirmConfirmButton");

let isModalOpen = false;

function updateConfirmButtonState() {
    confirmButton.disabled = !(check1.checked && check2.checked);
}

function openModal() {
    check1.checked = false;
    check2.checked = false;
    updateConfirmButtonState();
    overlay.hidden = false;
    isModalOpen = true;
    check1.focus();
}

function closeModal() {
    overlay.hidden = true;
    isModalOpen = false;
    submitButton.focus();
}

if (requiresReconfirmation) {
    check1.addEventListener('change', updateConfirmButtonState);
    check2.addEventListener('change', updateConfirmButtonState);

    backButton.addEventListener('click', () => {
        closeModal();
    });

    confirmButton.addEventListener('click', () => {
        if (confirmButton.disabled) {
            return;
        }
        confirmButton.disabled = true;
        closeModal();
        form.submit();
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && isModalOpen) {
            closeModal();
        }
    });
}

submitButton.addEventListener('click', (e) => {
    if (requiresReconfirmation) {
        e.preventDefault();
        if (!isModalOpen) {
            openModal();
        }
        return;
    }
    const isConfirmed = confirm("この内容で振込を確定します");
    console.log(isConfirmed);
    if (!isConfirmed) {
        e.preventDefault();
    }
});
