document.addEventListener("DOMContentLoaded", () => {
    const recentTransferCards = document.querySelectorAll(".recent-transfer-card");

    recentTransferCards.forEach((card) => {card.addEventListener("click", () => {

            document.getElementById("bankName").value = card.dataset.bankName;
            document.getElementById("branchName").value = card.dataset.branchName;
            document.getElementById("bankAccountNum").value = card.dataset.accountNum;
            document.getElementById("name").value = card.dataset.accountName;


            const accountTypeRadios = document.querySelectorAll('input[name="bankAccountType"]');
            accountTypeRadios.forEach((radio) => {radio.checked = radio.value === card.dataset.accountType;});
            recentTransferCards.forEach((otherCard) => {otherCard.classList.remove("selected");});
            card.classList.add("selected");});


    });
});