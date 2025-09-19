const initialProfilePicBase64 = /*[[${profilepic}]]*/ null;
const defaultProfilePicUrl = /*[[@{/tempscripts/images/defaultprofile.jpg}]]*/ '/tempscripts/images/defaultprofile.jpg';
const initialImageSrc = initialProfilePicBase64 ? 'data:image/jpeg;base64,' + initialProfilePicBase64 : defaultProfilePicUrl;

document.addEventListener('DOMContentLoaded', () => {
    const userloginForm = document.getElementById('userloginfid');
    const usermobileInput = document.getElementById('usermobile');
    const usernameInput = document.getElementById('username');
    const emailidInput = document.getElementById('emailid');
    const useridInput = document.getElementById('userid'); // ✅ Added this line
    const msg1Span = document.getElementById('msg1');
    const submitButton = document.getElementById('submit');
    const file1Input = document.getElementById('file1');

    usermobileInput.addEventListener('input', () => {
        msg1Span.textContent = "";
        const val = usermobileInput.value;
        usermobileInput.value = val.replace(/[^\d]/g, '');
    });

    usermobileInput.addEventListener('blur', () => {
        const m = usermobileInput.value;
        msg1Span.textContent = m.length !== 10 ? "Mobile no. should be exactly 10 digits" : "";
    });

    usernameInput.addEventListener('input', () => {
        usernameInput.value = usernameInput.value.replace(/[^a-zA-Z.\s]/g, '');
    });

    function isValidEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }

    function isValidFile(file) {
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
        const maxSizeKB = 500;
        if (!allowedTypes.includes(file.type)) {
            alert("Invalid file type. Only JPG, JPEG, or PNG allowed.");
            return false;
        }
        if ((file.size / 1024) > maxSizeKB) {
            alert(`File is too large. Max allowed size is ${maxSizeKB} KB.`);
            return false;
        }
        return true;
    }

    userloginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const m = usermobileInput.value;
        const name = usernameInput.value.trim();
        const emailVal = emailidInput.value.trim();

        // Validate inputs
        if (!m || m.length !== 10) {
            msg1Span.textContent = "Mobile number must be 10 digits.";
            usermobileInput.focus();
            return;
        }

        if (!name) {
            alert("Name is required.");
            usernameInput.focus();
            return;
        }

        if (!emailVal) {
            alert("E-mail id is required.");
            emailidInput.focus();
            return;
        } else if (!isValidEmail(emailVal)) {
            alert("Please enter a valid e-mail address.");
            emailidInput.focus();
            return;
        }

        // Validate file input
        if (file1Input.files.length > 0) {
            const file = file1Input.files[0];
            if (!isValidFile(file)) {
                file1Input.value = '';
                return;
            }
        }

        const formData = new FormData(userloginForm);
        const submitUrl = /*[[@{/nerie/users/profile/update}]]*/ '/nerie/users/profile/update';

        submitButton.disabled = true;
        submitButton.textContent = 'SAVING...';

        try {
            const response = await fetch(submitUrl, {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.text();
            switch (data) {
                case "2":
                    alert("Successfully Saved!");
                    window.location.reload();
                    break;
                case "1":
                    alert("Email ID already exists");
                    break;
                case "4":
                    alert("Uploaded file is not allowed. Kindly check file type or filename.");
                    break;
                default:
                    alert(`Save Failed! Server response: ${data}`);
            }
        } catch (error) {
            console.error("Fetch Error:", error);
            alert("An error occurred while saving. Please try again later.");
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'SUBMIT';
        }
    });

    // --- Email existence check ---
    emailidInput.addEventListener('focusout', async () => {
        const email = emailidInput.value.trim();
        if (!email || !useridInput) return;

        const re = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
        if (!re.test(email)) {
            alert("Please enter a valid Email ID format.");
            //emailidInput.focus();
            return;
        }

        const checkEmailUrl = /*[[@{/nerie/users/profile/check-email}]]*/ '/nerie/users/profile/check-email';
        const currentUserId = useridInput.value;

        try {
            const response = await fetch(checkEmailUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `emailid=${encodeURIComponent(email)}&userid=${encodeURIComponent(currentUserId)}`
            });

            if (!response.ok) {
                console.error('HTTP error during email check!', response);
                return;
            }

            const data = await response.text();
            if (data === "1") {
                alert("This Email ID is already registered to another user.");
            }
        } catch (error) {
            console.error("Fetch Error during email check:", error);
        }
    });
});
