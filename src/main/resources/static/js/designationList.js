function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

function showModalAndRedirect(message, url) {
    showModalAlert(message);
    $('#feedbackModal').one('hidden.bs.modal', function () {
        window.location.href = url;
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const designationTable = document.getElementById('designationtable');
    const designationTableBody = designationTable?.querySelector('tbody');
    const addDesignationBtn = document.getElementById('addDesignationBtn');
    const designationModal = document.getElementById('office-modal');
    const designationForm = document.getElementById('mdesignationfid');
    const modalTitle = document.getElementById('designationModalLabel');
    const designationCodeInput = document.getElementById('designationcode');
    const designationNameInput = document.getElementById('designationname');
    const designationTypeSelect = document.getElementById('isparticipantdesignation');

    const allowAlphabetsOnly = (event) => {
        if (event.key.length === 1 && !/^[a-zA-Z. ]$/.test(event.key)) {
            event.preventDefault();
        }
    };
    designationNameInput?.addEventListener('keypress', allowAlphabetsOnly);

    const resetForm = () => {
        designationForm.reset();
        designationForm.classList.remove('was-validated');
        designationCodeInput.value = '';
    };

    const populateFormForEdit = (button) => {
        resetForm();
        if(modalTitle) modalTitle.querySelector('u').textContent = 'Edit Designation';

        const data = button.dataset;
        designationCodeInput.value = data.designationcode || '';
        designationNameInput.value = data.designationname || '';
        designationTypeSelect.value = data.isparticipantdesignation || '';
    };

    designationTableBody?.addEventListener('click', (event) => {
        const editButton = event.target.closest('.designationbtn');
        if (editButton) {
            populateFormForEdit(editButton);
        }
    });

    addDesignationBtn?.addEventListener('click', () => {
        resetForm();
        if(modalTitle) modalTitle.querySelector('u').textContent = 'Add Designation';
    });

    const handleFormSubmit = async (event) => {
        event.preventDefault();

        if (!designationForm.checkValidity()) {
            event.stopPropagation();
            designationForm.classList.add('was-validated');
            return;
        }
        designationForm.classList.add('was-validated');

        const submitButton = document.getElementById('submitDesignationForm');
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';

        try {
            const response = await fetch(designationForm.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(new FormData(designationForm)).toString()
            });

            const responseBody = await response.text();

            if (!response.ok) {
                 if (response.status === 401) {
                    showModalAndRedirect("Authentication error. Please log in again.", '/nerie/login');
                 } else {
                    throw new Error(`Server error! Status: ${response.status}`);
                 }
                 return;
            }

            switch (responseBody.trim()) {
                case "1":
                    showModalAlert("Designation Already Exists!");
                    designationNameInput.focus();
                    designationNameInput.value = "";
                    designationForm.classList.remove('was-validated');
                    break;
                case "2":
                    $(designationModal).modal('hide');
                    $(designationModal).one('hidden.bs.modal', function() {
                         showModalAndRedirect("Successfully Saved!", window.location.href);
                    });
                    break;
                case "3":
                    showModalAlert("Designation cannot be empty!");
                    designationNameInput.focus();
                    break;
                case "4":
                    showModalAlert("Designation should be 1-100 characters long!");
                    designationNameInput.focus();
                    break;
                default:
                    showModalAlert("Save Failed! Unexpected response from server.");
                    break;
            }

        } catch (error) {
            showModalAlert(`An error occurred: ${error.message}`);
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'SUBMIT';
        }
    };

    designationForm?.addEventListener('submit', handleFormSubmit);

    if (typeof $ !== 'undefined' && $.fn.modal) {
        $(designationModal).on('hidden.bs.modal', resetForm);
    }

    if (typeof $ !== 'undefined' && $.fn.DataTable) {
        try {
            $('#designationtable').DataTable({
                dom: 'Blfrtip',
                pageLength: 10,
                lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
                buttons: [
                    {
                        extend: 'excelHtml5',
                        text: '<i class="fa fa-file-excel-o"></i> Excel',
                        title: 'Designations',
                        exportOptions: {
                            columns: ':visible:not(.noExport)'
                        },
                        className: 'btn btn-sm btn-outline-success'
                    }
                ]
            });
        } catch (e) {
            console.error('Could not initialize the designations table.', e);
            showModalAlert('Error: Could not initialize the list table.');
        }
    }
});