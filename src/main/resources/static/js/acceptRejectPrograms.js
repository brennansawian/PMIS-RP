$(document).ready(function() {
    console.log("Participant Action JS Loaded");

    // =================================================================
    // MODAL HELPER FUNCTION
    // =================================================================

    /**
     * Shows a generic feedback modal with a message and title.
     * @param {string} message - The message to display in the modal body.
     * @param {string} [title='Message'] - The title for the modal header.
     * @param {function} [onOkCallback=null] - A function to run when the OK button is clicked.
     */
    function showFeedbackModal(message, title = 'Message', onOkCallback = null) {
        $('#feedbackModalLabel').text(title);
        $('#feedbackModalBody').html(message);

        const okButton = $('#modalOkButton');
        // Remove previous event handlers to avoid multiple executions
        okButton.off('click');

        if (onOkCallback && typeof onOkCallback === 'function') {
            // We also need to handle the case where the modal is closed via the 'x' or backdrop click
            $('#feedbackModal').off('hidden.bs.modal').on('hidden.bs.modal', function () {
                onOkCallback();
                // Clean up the event listener after it runs
                $(this).off('hidden.bs.modal');
            });
        }

        // Ensure the footer is the default one
        $('#feedbackModal .modal-footer').html(
            '<button type="button" id="modalOkButton" class="btn btn-primary" data-dismiss="modal">OK</button>'
        );

        $('#feedbackModal').modal('show');
    }

    // =================================================================
    // MAIN LOGIC
    // =================================================================

    const approveProgramTable = document.getElementById('approveprogram');
    const acceptForm = document.getElementById('acceptProgramForm');
    const rejectForm = document.getElementById('rejectProgramForm');

    if (approveProgramTable) {
        approveProgramTable.addEventListener('click', (event) => {
            const target = event.target;

            const acceptButton = target.closest('.acceptbtn');
            if (acceptButton && acceptForm) {
                const programName = acceptButton.dataset.programname;
                const programCode = acceptButton.dataset.programcode;

                document.getElementById('acceptprogramname').textContent = programName;
                document.getElementById('acceptprogramcode').value = programCode;
                document.getElementById('acceptremarks').value = '';

                acceptForm.dataset.programname = programName;
            }

            const rejectButton = target.closest('.rejectbtn');
            if (rejectButton && rejectForm) {
                const programName = rejectButton.dataset.programname;
                const programCode = rejectButton.dataset.programcode;

                document.getElementById('reject_program_name_display').textContent = programName;
                document.getElementById('reject_program_code').value = programCode;
                document.getElementById('reject_remarks').value = '';

                rejectForm.dataset.programname = programName;
            }
        });
    }

    const handleFormSubmit = async (form, url, actionType, programName) => {
        if (!url) {
            console.error("Form action URL is not set. Cannot submit.");
            showFeedbackModal("Configuration error: Form submission URL is missing.", "Error");
            return;
        }

        const phaseid = form.querySelector('input[name="phaseid"]').value;
        const remarks = form.querySelector('textarea[name="remarks"]').value;

        if (!phaseid) {
            showFeedbackModal(`Error: Program Code is missing.`, "Error");
            return;
        }

        const formData = new URLSearchParams();
        formData.append('phaseid', phaseid);
        formData.append('remarks', remarks);

        // Hide the form modal (Accept/Reject) before showing feedback
        $(form).closest('.modal').modal('hide');

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString(),
            });

            const responseBody = await response.text();

            if (response.ok && responseBody === "2") {
                // Show success feedback and refresh the page on OK or close
                showFeedbackModal("Successfully Saved!", "Success", () => {
                    window.location.reload();
                });
            } else {
                console.error(`${actionType} failed. Status: ${response.status}, Body: ${responseBody}`);
                showFeedbackModal(`Save Failed! Server responded with: ${responseBody || 'No response body'}`, "Error");
            }
        } catch (error) {
            console.error(`Network or other error during ${actionType} submission:`, error);
            showFeedbackModal(`An error occurred. Please check the console for details.`, "Error");
        }
    };

    if (acceptForm) {
        acceptForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const programName = event.target.dataset.programname || 'this program';
            await handleFormSubmit(acceptForm, acceptForm.getAttribute('action'), 'Accept', programName);
        });
    }

    if (rejectForm) {
        rejectForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const programName = event.target.dataset.programname || 'this program';
            await handleFormSubmit(rejectForm, rejectForm.getAttribute('action'), 'Reject', programName);
        });
    }

    // Initialize DataTables
    if ($.fn.DataTable && $('#approveprogram').length) {
       try {
            $('#approveprogram').DataTable({
                dom: 'lfrtip',
                pageLength: 5,
                lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]]
            });
             console.log("DataTables initialized for #approveprogram.");
        } catch (e) {
            console.error("Error initializing DataTables:", e);
        }
    } else {
        console.warn("jQuery, DataTables, or the #approveprogram element not found. DataTables not initialized.");
    }
});