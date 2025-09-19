document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('userloginfid');
    const userIdInput = document.getElementById('userid');
    const oldPasswordInput = document.getElementById('olduserpassword');
    const newPasswordInput = document.getElementById('userpassword');
    const confirmPasswordInput = document.getElementById('confirmpassword');
    const submitButton = document.getElementById('submitBtn');
    const resetButton = document.getElementById('customResetBtn');

    const oldPasswordError = document.getElementById('oldPasswordError');
    const newPasswordError = document.getElementById('newPasswordError');
    const confirmPasswordError = document.getElementById('confirmPasswordError');

    const strengthPercent = document.getElementById('percent');
    const strengthResults = document.getElementById('results');
    const strengthColorbar = document.getElementById('colorbar');

    // --- Validation Functions ---
    async function checkOldPasswordMatch() {
        clearError(oldPasswordError);
        const oldPassword = oldPasswordInput.value;
        if (oldPassword.trim().length === 0) {
            return true;
        }

        const checkUrl = '/nerie/users/check-old-password';

        try {
            const formData = new URLSearchParams();
            formData.append('olduserpassword', oldPassword);

            const response = await fetch(checkUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.text();

            if (result === "0") {
                alert("Old Password not match");
                showError(oldPasswordError, "Old Password does not match.");
                oldPasswordInput.value = "";
                oldPasswordInput.focus();
                return false;
            }
            return true;
        } catch (error) {
            console.error("Error checking old password:", error);
            alert("Error verifying old password. Please try again.");
            showError(oldPasswordError, "Could not verify old password.");
            return false;
        }
    }

    // 2. Check New Password Requirements
    function checkPasswordRequirements() {
        clearError(newPasswordError);
        const password = newPasswordInput.value;
         if (password.trim().length === 0) return true;

        const re = /(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}/;
        if (!re.test(password)) {
            alert("Password doesn't match requirement!!! Password must contain at least one number, one lowercase, one uppercase letter, and be at least 8 characters long.");
            showError(newPasswordError, "Requires number, lowercase, uppercase, min 8 chars.");
            newPasswordInput.value = "";
            updatePasswordStrengthUI(0, "");
            newPasswordInput.focus();
            return false;
        }
        return true;
    }

    // 3. Check if New Password contains User ID
    function checkUserIdInPassword() {
        clearError(newPasswordError);
        const userId = userIdInput.value?.toUpperCase() || '';
        const password = newPasswordInput.value?.toUpperCase() || '';
         if (password.trim().length === 0 || userId.trim().length === 0) return true;

        if (password.includes(userId)) {
            alert("Password should not contain User Id.");
            showError(newPasswordError, "Password cannot contain your User ID.");
            newPasswordInput.value = "";
            updatePasswordStrengthUI(0, "");
            newPasswordInput.focus();
            return false;
        }
        return true;
    }

    // 4. Check if New Password is the same as Old Password
    function checkOldNewPassword() {
        clearError(newPasswordError);
        const oldPassword = oldPasswordInput.value;
        const newPassword = newPasswordInput.value;
         if (newPassword.trim().length === 0 || oldPassword.trim().length === 0) return true;

        if (oldPassword === newPassword) {
            alert("New Password should not match with old password");
            showError(newPasswordError, "New password cannot be the same as the old password.");
            newPasswordInput.value = "";
            updatePasswordStrengthUI(0, "");
            newPasswordInput.focus();
            return false;
        }
        return true;
    }

    // 5. Check if Confirm Password matches New Password
    function checkConfirmPassword() {
        clearError(confirmPasswordError);
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        if (confirmPassword.trim().length === 0) return true;

        if (newPassword.trim().length === 0 && confirmPassword.trim().length > 0) {
            alert("Please Enter new password first");
            showError(confirmPasswordError, "Enter the new password above first.");
            confirmPasswordInput.value = "";
            newPasswordInput.focus();
            return false;
        }

        if (newPassword !== confirmPassword) {
            alert("Confirm Password does not match");
            showError(confirmPasswordError, "Passwords do not match.");
            confirmPasswordInput.value = "";
            confirmPasswordInput.focus();
            return false;
        }
        return true;
    }

    // --- Password Strength Calculation & UI Update ---
    function calculatePasswordStrength(password) {
         if (!password || password.length === 0) return { score: 0, text: "" };

        let score = 0;
        // Criteria
        if (password.length >= 8) score += 25; else score += password.length * 3; // Basic length points
        if (password.match(/[a-z]/)) score += 15; // Lowercase
        if (password.match(/[A-Z]/)) score += 20; // Uppercase
        if (password.match(/\d/)) score += 20;   // Numbers
        if (password.match(/[^a-zA-Z0-9]/)) score += 20; // Special chars

        score = Math.min(score, 100);

        let text = "Weak";
        if (score >= 85) text = "Very Strong";
        else if (score >= 70) text = "Strong";
        else if (score >= 50) text = "Medium";
        else if (score >= 25) text = "Weak";
        else text = "Very Weak";


        return { score: score, text: text };
    }

    function updatePasswordStrengthUI(score, text) {
        strengthPercent.textContent = `${score}%`;
        strengthResults.textContent = text;

        let color = "#d9534f"; // Red (Weak)
        if (score >= 85) color = "#5cb85c";
        else if (score >= 70) color = "#4cae4c";
        else if (score >= 50) color = "#f0ad4e";
        else if (score >= 25) color = "#f0ad4e";

        strengthColorbar.style.width = `${score}%`;
        strengthColorbar.style.backgroundColor = color;
    }

    function showError(element, message) {
        if (element) {
            element.textContent = message;
            element.style.display = 'block';
        }
    }

    function clearError(element) {
         if (element) {
            element.textContent = '';
            element.style.display = 'none';
         }
    }
     function clearAllErrors() {
        clearError(oldPasswordError);
        clearError(newPasswordError);
        clearError(confirmPasswordError);
     }

    oldPasswordInput.addEventListener('blur', async () => {
        await checkOldPasswordMatch();
    });

    newPasswordInput.addEventListener('keyup', () => {
         clearError(newPasswordError);
        const password = newPasswordInput.value;
        const { score, text } = calculatePasswordStrength(password);
        updatePasswordStrengthUI(score, text);

        if (oldPasswordInput.value.trim().length === 0 && password.trim().length > 0) {
             alert("Please Enter old password first");
             newPasswordInput.value = "";
             updatePasswordStrengthUI(0, "");
             oldPasswordInput.focus();
        }
    });

    newPasswordInput.addEventListener('blur', () => {
        if (!checkUserIdInPassword()) return;
        if (!checkPasswordRequirements()) return;
        if (!checkOldNewPassword()) return;
    });

    confirmPasswordInput.addEventListener('keyup', () => {
         clearError(confirmPasswordError);
         if (newPasswordInput.value.trim().length === 0 && confirmPasswordInput.value.trim().length > 0) {
             alert("Please Enter new password first");
             confirmPasswordInput.value = "";
             newPasswordInput.focus();
         } else {}
    });

    confirmPasswordInput.addEventListener('blur', () => {
        checkConfirmPassword();
    });

    // Form Submission
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearAllErrors();
        let isValid = true;

        // 1. Required fields
        if (oldPasswordInput.value.trim() === '') {
            showError(oldPasswordError, 'Old Password is required.');
            isValid = false;
        }
        if (newPasswordInput.value.trim() === '') {
            showError(newPasswordError, 'New Password is required.');
            isValid = false;
        }
        if (confirmPasswordInput.value.trim() === '') {
            showError(confirmPasswordError, 'Confirm Password is required.');
            isValid = false;
        }

        // 2. Run all validation functions again
        if (!checkUserIdInPassword()) isValid = false;
        if (!checkPasswordRequirements()) isValid = false;
        if (!checkOldNewPassword()) isValid = false;
        if (!checkConfirmPassword()) isValid = false;

        // 3. Re-check old password match
        if(isValid) {
            const isOldPasswordServerValid = await checkOldPasswordMatch();
            if (!isOldPasswordServerValid) {
                isValid = false;
            }
        }


        if (!isValid) {
            alert("Please fix the errors before submitting.");
            return; // Stop submission
        }

        submitButton.disabled = true;
        submitButton.textContent = 'SUBMITTING...';

        const formData = new FormData(form);

         const urlEncodedData = new URLSearchParams(formData).toString();

        try {
            const response = await fetch(form.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: urlEncodedData
            });

            if (!response.ok) {
               console.error("Server responded with status:", response.status);
               let errorMsg = "An unexpected error occurred.";
               if(response.status === 401) errorMsg = "Authentication failed. Please log in again.";
               else if (response.status === 400) errorMsg = "Invalid data submitted.";
               else if (response.status === 500) errorMsg = "Server error during password change.";
               alert(errorMsg);
               throw new Error(`HTTP error! status: ${response.status}`);
            }


            const result = await response.text();

            if (result === "2") {
                alert("Successfully Updated!!!");
                window.location.href = '/nerie/users/change-password';
            } else if (result === "1") {
                alert("Save Failed!!! Old Password not match!!!");
                showError(oldPasswordError, "Old Password does not match.");
                oldPasswordInput.focus();
            } else if (result === "3") {
                 alert("Save Failed!!! New password cannot be the same as the old password.");
                 showError(newPasswordError, "New password cannot be the same as the old password.");
                 newPasswordInput.value = "";
                 confirmPasswordInput.value = "";
                 updatePasswordStrengthUI(0, "");
                 newPasswordInput.focus();
            } else if (result === "5") {
                 alert("Save Failed!!! Password should not contain User Id.");
                 showError(newPasswordError, "Password cannot contain your User ID.");
                 newPasswordInput.value = "";
                 confirmPasswordInput.value = "";
                 updatePasswordStrengthUI(0, "");
                 newPasswordInput.focus();
            } else { // Includes "0" or any other unexpected response
                alert("Save Failed!!! An unknown error occurred.");
            }

        } catch (error) {
            console.error("Error submitting form:", error);
            alert("Submission failed due to a network or server error. Please try again.");
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'SUBMIT';
        }
    });

    resetButton.addEventListener('click', () => {
        window.location.href = '/nerie/users/change-password';
    });

     const initialPassword = newPasswordInput.value;
     const { score: initialScore, text: initialText } = calculatePasswordStrength(initialPassword);
     updatePasswordStrengthUI(initialScore, initialText);

});