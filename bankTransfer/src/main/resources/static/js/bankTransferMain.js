document.addEventListener("DOMContentLoaded", () => {
    // 振込金額・振込手数料・合計引落金額を画面上でリアルタイム表示する
    // 手数料の計算ルールは ApplyBankTransferService.calculateFee と一致させること
    const formatYen = (amount) => amount.toLocaleString('ja-JP');

    const updateFeeBreakdown = () => {
        const moneyInput = document.getElementById("money");
        const bankNameHidden = document.getElementById("bankName");
        const feeBreakdownMoney = document.getElementById("feeBreakdownMoney");
        const feeBreakdownFee = document.getElementById("feeBreakdownFee");
        const feeBreakdownTotal = document.getElementById("feeBreakdownTotal");
        if (!moneyInput || !bankNameHidden || !feeBreakdownMoney || !feeBreakdownFee || !feeBreakdownTotal) {
            return;
        }

        const money = Math.max(0, parseInt(moneyInput.value, 10) || 0);
        const myBankName = moneyInput.dataset.myBankName || "";
        const recipientBankName = bankNameHidden.value || "";

        let fee = 0;
        if (money > 0 && recipientBankName && recipientBankName !== myBankName) {
            fee = money >= 30000 ? 440 : 220;
        }

        feeBreakdownMoney.textContent = formatYen(money);
        feeBreakdownFee.textContent = formatYen(fee);
        feeBreakdownTotal.textContent = formatYen(money + fee);
    };

    // ステップ2・3の上部に、それまでに入力した振込先情報を表示する
    const updateInfoPreview = () => {
        const bankName = document.getElementById("bankName").value;
        const branchName = document.getElementById("branchName").value;
        const accountTypeRadio = document.querySelector('input[name="bankAccountType"]:checked');
        const accountType = accountTypeRadio ? accountTypeRadio.value : "";
        const bankAccountNum = document.getElementById("bankAccountNum").value;
        const name = document.getElementById("name").value;

        const step3Preview = document.getElementById("infoPreviewStep3");
        if (step3Preview) {
            step3Preview.textContent = [bankName, branchName].filter(Boolean).join('　');
        }

        const step4Preview = document.getElementById("infoPreviewStep4");
        if (step4Preview) {
            step4Preview.textContent = [bankName, branchName, accountType, bankAccountNum, name].filter(Boolean).join('　');
        }
    };

    const syncComboValue = (inputId, hiddenId, value) => {
        const input = document.getElementById(inputId);
        const hidden = document.getElementById(hiddenId);
        if (!input || !hidden) return;

        const nextValue = (value || '').trim();
        input.value = nextValue;
        hidden.value = nextValue;
    };

    let cameViaSkip = false;

    // 振込先を選択してください（新規／登録済み／過去）
    const newRecipientButton = document.getElementById("newRecipientButton");
    const favoriteMethodToggle = document.getElementById("favoriteMethodToggle");
    const recentMethodToggle = document.getElementById("recentMethodToggle");
    const favoriteCandidateList = document.getElementById("favoriteCandidateList");
    const recentCandidateList = document.getElementById("recentCandidateList");

    const closeCandidateLists = () => {
        if (favoriteCandidateList) favoriteCandidateList.hidden = true;
        if (recentCandidateList) recentCandidateList.hidden = true;
    };

    newRecipientButton && newRecipientButton.addEventListener("click", () => {
        closeCandidateLists();
        cameViaSkip = false;
        showStep(2);
    });

    favoriteMethodToggle && favoriteMethodToggle.addEventListener("click", () => {
        const willOpen = favoriteCandidateList.hidden;
        closeCandidateLists();
        favoriteCandidateList.hidden = !willOpen;
    });

    recentMethodToggle && recentMethodToggle.addEventListener("click", () => {
        const willOpen = recentCandidateList.hidden;
        closeCandidateLists();
        recentCandidateList.hidden = !willOpen;
    });

    // 「登録済み振込先」「過去の振込先」の候補カード
    const recipientCandidateCards = document.querySelectorAll(".recipient-candidate-list .recent-transfer-card");

    recipientCandidateCards.forEach((card) => {
        card.addEventListener("click", () => {
            syncComboValue("bankNameInput", "bankName", card.dataset.bankName);
            syncComboValue("branchNameInput", "branchName", card.dataset.branchName);
            document.getElementById("bankAccountNum").value = card.dataset.accountNum;
            document.getElementById("name").value = card.dataset.accountName;

            const accountTypeRadios = document.querySelectorAll('input[name="bankAccountType"]');
            accountTypeRadios.forEach((radio) => {
                radio.checked = radio.value === card.dataset.accountType;
            });
            recipientCandidateCards.forEach((otherCard) => {
                otherCard.classList.remove("selected");
            });
            card.classList.add("selected");
            updateFeeBreakdown();

            // 振込先情報入力ステップをスキップして金額入力ステップへ直行する
            cameViaSkip = true;
            showStep(4);
        });
    });

    const setupComboBox = (inputId, hiddenId, listId, comboId, options = {}) => {
        const { extraFilter, onChange } = options;
        const comboInput = document.getElementById(inputId);
        const comboHidden = document.getElementById(hiddenId);
        const comboList = document.getElementById(listId);
        const comboWrapper = document.getElementById(comboId);
        const comboToggle = comboWrapper ? comboWrapper.querySelector('.combo-toggle') : null;

        if (!comboInput || !comboHidden || !comboList || !comboWrapper) {
            return;
        }

        let items = Array.from(comboList.querySelectorAll('li'));
        let highlighted = -1;

        const refreshItems = () => {
            items = Array.from(comboList.querySelectorAll('li')).filter(li => li.style.display !== 'none');
        };

        const openList = () => {
            comboList.hidden = false;
        };

        const closeList = () => {
            comboList.hidden = true;
            highlighted = -1;
            items.forEach((item) => item.classList.remove('highlight'));
        };

        const filterList = (query) => {
            const normalized = (query || '').toLowerCase();
            Array.from(comboList.querySelectorAll('li')).forEach((li) => {
                const value = (li.dataset.value || li.textContent || '').toLowerCase();
                const matchesText = !normalized || value.includes(normalized);
                const matchesExtra = !extraFilter || extraFilter(li);
                li.style.display = (matchesText && matchesExtra) ? '' : 'none';
            });
            refreshItems();
        };

        const selectValue = (value) => {
            const nextValue = (value || '').trim();
            comboInput.value = nextValue;
            comboHidden.value = nextValue;
            closeList();
            if (onChange) onChange(nextValue);
            updateFeeBreakdown();
        };

        if (comboHidden.value) {
            comboInput.value = comboHidden.value;
        }

        comboInput.addEventListener('focus', () => {
            filterList(comboInput.value);
            openList();
        });

        comboInput.addEventListener('input', (event) => {
            comboHidden.value = event.target.value;
            filterList(event.target.value);
            openList();
            if (onChange) onChange(event.target.value);
            updateFeeBreakdown();
        });

        comboList.addEventListener('click', (event) => {
            const li = event.target.closest('li');
            if (!li) return;
            selectValue(li.dataset.value || li.textContent.trim());
        });

        comboToggle && comboToggle.addEventListener('click', () => {
            if (comboList.hidden) {
                filterList(comboInput.value);
                openList();
                comboInput.focus();
            } else {
                closeList();
            }
        });

        comboInput.addEventListener('keydown', (event) => {
            if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
                event.preventDefault();
                const visible = items;
                if (!visible.length) return;
                if (event.key === 'ArrowDown') highlighted = (highlighted + 1) % visible.length;
                if (event.key === 'ArrowUp') highlighted = (highlighted - 1 + visible.length) % visible.length;
                items.forEach((item) => item.classList.remove('highlight'));
                visible[highlighted].classList.add('highlight');
                visible[highlighted].scrollIntoView({ block: 'nearest' });
            } else if (event.key === 'Enter') {
                event.preventDefault();
                if (highlighted >= 0 && items[highlighted]) {
                    selectValue(items[highlighted].dataset.value || items[highlighted].textContent.trim());
                } else if (comboInput.value) {
                    selectValue(comboInput.value.trim());
                }
            } else if (event.key === 'Escape') {
                closeList();
            }
        });

        document.addEventListener('click', (event) => {
            if (!comboWrapper.contains(event.target)) {
                closeList();
            }
        });
    };

    // 銀行が変わったら支店の選択をクリアし、支店コンボは選択中の銀行の支店だけに絞り込む
    setupComboBox('bankNameInput', 'bankName', 'bankNameList', 'bankNameCombo', {
        onChange: () => {
            syncComboValue('branchNameInput', 'branchName', '');
        }
    });
    setupComboBox('branchNameInput', 'branchName', 'branchNameList', 'branchNameCombo', {
        extraFilter: (li) => {
            const selectedBankName = document.getElementById('bankName').value;
            return !selectedBankName || li.dataset.bankName === selectedBankName;
        }
    });

    // 金額欄: 表示用（3桁カンマ区切り）と送信用（数字のみ）の値を同期する
    const moneyDisplay = document.getElementById("moneyDisplay");
    const moneyHidden = document.getElementById("money");
    const syncMoneyDisplay = () => {
        if (!moneyDisplay || !moneyHidden) return;
        const digits = moneyDisplay.value.replace(/[^0-9]/g, "");
        moneyHidden.value = digits;
        moneyDisplay.value = digits ? Number(digits).toLocaleString("ja-JP") : "";
        updateFeeBreakdown();
    };
    if (moneyDisplay && moneyHidden && moneyHidden.value) {
        moneyDisplay.value = Number(moneyHidden.value).toLocaleString("ja-JP");
    }
    moneyDisplay && moneyDisplay.addEventListener("input", syncMoneyDisplay);
    updateFeeBreakdown();

    const form = document.getElementById("bankTransferForm");
    const wizardSteps = Array.from(document.querySelectorAll(".wizard-step"));
    const prevButtons = Array.from(document.querySelectorAll("[data-prev-step]"));
    const nextButtons = Array.from(document.querySelectorAll("[data-next-step]"));
    let currentStepIndex = 0;

    const attachLiveValidation = () => {
        const bankNameInput = document.getElementById("bankNameInput");
        const branchNameInput = document.getElementById("branchNameInput");
        const bankAccountNum = document.getElementById("bankAccountNum");
        const nameField = document.getElementById("name");
        const moneyDisplayField = document.getElementById("moneyDisplay");
        const moneyHiddenField = document.getElementById("money");
        const transferDate = document.getElementById("transferDateTime");

        bankNameInput && bankNameInput.addEventListener("input", () => {
            if (document.getElementById("bankName").value.trim()) clearFieldError("bankNameInput");
        });
        branchNameInput && branchNameInput.addEventListener("input", () => {
            if (document.getElementById("branchName").value.trim()) clearFieldError("branchNameInput");
        });
        document.querySelectorAll('input[name="bankAccountType"]').forEach((radio) => {
            radio.addEventListener("change", () => {
                if (document.querySelector('input[name="bankAccountType"]:checked')) {
                    clearFieldError("bankAccountType");
                }
            });
        });
        bankAccountNum && bankAccountNum.addEventListener("input", () => {
            if (bankAccountNum.value.trim() && /^[0-9]{1,7}$/.test(bankAccountNum.value.trim())) {
                clearFieldError("bankAccountNum");
            }
        });
        nameField && nameField.addEventListener("input", () => {
            if (nameField.value.trim() && /^[ｦ-ﾟ ]+$/.test(nameField.value.trim())) {
                clearFieldError("name");
            }
        });
        moneyDisplayField && moneyDisplayField.addEventListener("input", () => {
            if (moneyHiddenField.value && Number(moneyHiddenField.value) > 0 && Number(moneyHiddenField.value) <= Number(moneyHiddenField.max || 999999999)) {
                clearFieldError("money");
            }
        });
        transferDate && transferDate.addEventListener("input", () => {
            if (transferDate.value) {
                const today = new Date();
                const currentDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
                const selectedDate = new Date(transferDate.value + "T00:00:00");
                if (selectedDate >= currentDay) {
                    clearFieldError("transferDateTime");
                }
            }
        });
    };

    // ステップの並び: 0=口座選択 1=振込先選択方法 2=金融機関/支店 3=科目/口座番号/口座名義 4=金額/指定日
    const pageProgressSteps = document.getElementById("pageProgressSteps");
    const progressStepClasses = ["step-1", "step-2", "step-3", "step-4", "step-5"];

    const setFieldError = (fieldId, message) => {
        const field = document.getElementById(fieldId);
        const errorEl = document.querySelector(`[data-error-for="${fieldId}"]`);
        if (field) {
            field.classList.toggle("is-invalid", Boolean(message));
        }
        if (fieldId === "bankAccountType") {
            const group = document.getElementById("bankAccountTypeGroup");
            if (group) {
                group.classList.toggle("is-invalid", Boolean(message));
            }
        }
        if (fieldId === "money") {
            const moneyDisplayField = document.getElementById("moneyDisplay");
            if (moneyDisplayField) {
                moneyDisplayField.classList.toggle("is-invalid", Boolean(message));
            }
        }
        if (errorEl) {
            errorEl.textContent = message || "";
        }
    };

    const clearFieldError = (fieldId) => setFieldError(fieldId, "");

    const showStep = (index) => {
        currentStepIndex = Math.max(0, Math.min(index, wizardSteps.length - 1));
        wizardSteps.forEach((step, stepIndex) => {
            step.classList.toggle("active", stepIndex === currentStepIndex);
        });
        updateInfoPreview();
        // プログレスバー: 金融機関/支店(2)→1, 口座情報(3)→2, 振込内容(4)→3。口座選択・振込先選択(0,1)では非表示
        if (pageProgressSteps) {
            if (currentStepIndex < 2) {
                pageProgressSteps.hidden = true;
            } else {
                pageProgressSteps.hidden = false;
                pageProgressSteps.classList.remove(...progressStepClasses);
                pageProgressSteps.classList.add(`step-${currentStepIndex - 1}`);
            }
        }
    };

    const validateCurrentStep = () => {
        const step = wizardSteps[currentStepIndex];
        if (!step) {
            return true;
        }

        if (currentStepIndex === 0 || currentStepIndex === 1) {
            return true;
        }

        if (currentStepIndex === 2) {
            const bankName = document.getElementById("bankName").value.trim();
            const branchName = document.getElementById("branchName").value.trim();
            let isValid = true;

            if (!bankName) {
                setFieldError("bankNameInput", "金融機関名を入力してください。");
                isValid = false;
            } else {
                clearFieldError("bankNameInput");
            }

            if (!branchName) {
                setFieldError("branchNameInput", "支店名を入力してください。");
                isValid = false;
            } else {
                clearFieldError("branchNameInput");
            }

            return isValid;
        }

        if (currentStepIndex === 3) {
            const accountType = document.querySelector('input[name="bankAccountType"]:checked');
            const bankAccountNum = document.getElementById("bankAccountNum");
            const nameField = document.getElementById("name");
            let isValid = true;

            if (!accountType) {
                setFieldError("bankAccountType", "科目名を選択してください。");
                isValid = false;
            } else {
                clearFieldError("bankAccountType");
            }

            const bankAccountNumValue = bankAccountNum.value.trim();
            if (!bankAccountNumValue) {
                setFieldError("bankAccountNum", "口座番号を入力してください。");
                isValid = false;
            } else if (!/^[0-9]{1,7}$/.test(bankAccountNumValue)) {
                setFieldError("bankAccountNum", "口座番号は半角数字7桁以内で入力してください。");
                isValid = false;
            } else {
                clearFieldError("bankAccountNum");
            }

            const nameValue = nameField.value.trim();
            if (!nameValue) {
                setFieldError("name", "口座名義を入力してください。");
                isValid = false;
            } else if (!/^[ｦ-ﾟ ]+$/.test(nameValue)) {
                setFieldError("name", "口座名義は半角カタカナで入力してください。");
                isValid = false;
            } else {
                clearFieldError("name");
            }

            return isValid;
        }

        if (currentStepIndex === 4) {
            const moneyField = document.getElementById("money");
            const transferDate = document.getElementById("transferDateTime");
            let isValid = true;
            const moneyValue = moneyField.value.trim();

            if (!moneyValue) {
                setFieldError("money", "振込金額を入力してください。");
                isValid = false;
            } else {
                const amount = Number(moneyValue);
                const balance = Number(moneyField.max || 0);
                if (amount <= 0) {
                    setFieldError("money", "振込金額は1円以上で入力してください。");
                    isValid = false;
                } else if (balance > 0 && amount > balance) {
                    setFieldError("money", "振込金額が口座残高を超えています");
                    isValid = false;
                } else {
                    clearFieldError("money");
                }
            }

            const dateValue = transferDate.value;
            if (!dateValue) {
                setFieldError("transferDateTime", "振込指定日を入力してください。");
                isValid = false;
            } else {
                const today = new Date();
                const currentDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
                const selectedDate = new Date(dateValue + "T00:00:00");
                if (selectedDate < currentDay) {
                    setFieldError("transferDateTime", "過去の日付は指定できません。");
                    isValid = false;
                } else {
                    clearFieldError("transferDateTime");
                }
            }

            return isValid;
        }

        return true;
    };

    prevButtons.forEach((button) => {
        button.addEventListener("click", () => {
            // 金額入力ステップへ「登録済み/過去」からスキップして来た場合は、振込先選択ステップへ戻る
            if (button.id === "moneyStepBackButton" && cameViaSkip) {
                showStep(1);
                return;
            }
            if (currentStepIndex > 0) {
                showStep(currentStepIndex - 1);
            }
        });
    });

    nextButtons.forEach((button) => {
        button.addEventListener("click", () => {
            if (!validateCurrentStep()) {
                return;
            }
            showStep(currentStepIndex + 1);
        });
    });

    form.addEventListener("submit", (event) => {
        if (!validateCurrentStep()) {
            event.preventDefault();
        }
    });

    form.addEventListener("reset", () => {
        setTimeout(() => {
            cameViaSkip = false;
            recipientCandidateCards.forEach((card) => card.classList.remove("selected"));
            closeCandidateLists();
            showStep(0);
            updateFeeBreakdown();
        }, 0);
    });

    attachLiveValidation();
    showStep(currentStepIndex);
});
