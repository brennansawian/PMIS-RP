document.addEventListener('DOMContentLoaded', () => {

// --- Check for Bootstrap Modal ---
if (typeof $ === 'undefined' || typeof $.fn.modal === 'undefined') {
    // TODO added if needed, e.g., disable buttons
}

const myProgramTable = document.getElementById('myProgramsTable');
if (!myProgramTable) {
    return;
}

// --- Helper Functions ---
const showLoading = (element) => {
    if (element) {
        element.innerHTML = '<p class="text-center fst-italic text-muted py-3">Loading...</p>';
    }
};

const showError = (element, message = 'Failed to load data.') => {
    if (element) {
        element.innerHTML = `<p class="text-center text-danger py-3">${message}</p>`;
    }
};

const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}-${month}-${year}`;
    } catch (e) {
        return dateString;
    }
};

const formatTime = (timeString) => {
     if (!timeString || typeof timeString !== 'string' || !timeString.includes(':')) return 'N/A';
     try{
         const parts = timeString.split(':');
         return `${parts[0]}:${parts[1]}`;
     } catch (e) {
         return timeString;
     }
};

// --- Fetch and Render Functions ---

// 1. Fetch Material Data
const fetchMaterialData = async (phaseid, targetDiv) => {
    showLoading(targetDiv);
    try {
        const response = await fetch(`/nerie/program-materials/list`, {
             method: 'POST',
             headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
             body: new URLSearchParams({ phaseid })
         });

        if (!response.ok) {
            let errorText = `HTTP error! status: ${response.status} - ${response.statusText}`;
             try {
                const errorBody = await response.text();
                if(errorBody) { errorText += `\nServer response: ${errorBody}`; }
             } catch(e) { /* Ignore */ }
            throw new Error(errorText);
        }

        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
            throw new Error(`Could not parse server response as JSON. ${jsonError.message}.`);
        }


        let tableHtml = `<table class="table table-striped table-bordered center-sans-serif fs-12" id="materialDataTable" style="width:100%;">
                <thead class="fs-14">
                    <tr>
                        <th class="w-1">Sl No.</th>
                        <th>Material Description</th>
                        <th>Upload Date</th>
                        <th>File</th>
                    </tr>
                </thead>
                <tbody>`;

        if (!data || !Array.isArray(data) || data.length === 0) {
            // DataTable will show 'No data available' if tbody is empty
        } else {
            data.forEach((item, index) => {
                if (!Array.isArray(item) || item.length < 3) return; // Basic validation
                const materialId = item[0];
                const description = item[1] || 'N/A';
                const uploadDate = formatDate(item[2]);
                const downloadUrl = `/nerie/myprogram/download-material?coursematerialid=${encodeURIComponent(materialId)}`;

                tableHtml += `
                    <tr>
                        <td>${index + 1}</td>
                        <td>${description}</td>
                        <td class="text-nowrap">${uploadDate}</td>
                        <td><a href="${downloadUrl}" class="btn btn-sm btn-outline-primary" download>
                                <i class="fa fa-download"></i> Download
                            </a>
                        </td>
                    </tr>`;
            });
        }

        tableHtml += `</tbody></table>`;
        targetDiv.innerHTML = tableHtml;

         // --- DataTables Initialization for the Modal Table ---
         if (typeof $ !== 'undefined' && $.fn.DataTable) {
              const materialTableElement = $('#materialDataTable');

              if (materialTableElement.length) {
                   if ($.fn.DataTable.isDataTable(materialTableElement)) {
                        materialTableElement.DataTable().destroy();
                   }
                   try {
                        materialTableElement.DataTable({
                            pageLength: 5,
                            lengthMenu: [[5, 10, 25, -1], [5, 10, 25, "All"]],
                            responsive: true,
                            dom: 'lfrtip',
                            language: {
                                processing: "Processing...",
                                search: "Search:",
                                lengthMenu: "Show _MENU_ entries",
                                info: "Showing _START_ to _END_ of _TOTAL_ entries",
                                infoEmpty: "Showing 0 to 0 of 0 entries",
                                infoFiltered: "(filtered from _MAX_ total entries)",
                                zeroRecords: "No matching records found",
                                emptyTable: "No data available in table",
                                paginate: { first: "First", last: "Last", next: "Next", previous: "Previous" }
                            }
                        });
                   } catch(e) {
                        showError(targetDiv, 'Error displaying material list.');
                   }
              }
         }

    } catch (error) {
        showError(targetDiv, `Could not load materials: ${error.message}`);
    }
};

// 2. Fetch Daily Feedback Subjects
const fetchDailyFeedbackSubjects = async (programcode, phaseid, targetTbody) => {
    showLoading(targetTbody);
    try {
        const formData = new URLSearchParams();
        formData.append('programcode', programcode);
        formData.append('phaseid', phaseid);

        const response = await fetch(`/nerie/participant/feedback/daily-feedback`, {
             method: 'POST',
             headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
             body: formData.toString()
         });

        if (!response.ok) {
            let errorText = `HTTP error! status: ${response.status} - ${response.statusText}`;
             try {
                const errorBody = await response.text();
                if(errorBody) { errorText += `\nServer response: ${errorBody}`; }
             } catch(e) { /* Ignore */ }
            throw new Error(errorText);
        }

        let data;
         try {
            data = await response.json();
        } catch (jsonError) {
             throw new Error(`Could not parse server response as JSON. ${jsonError.message}.`);
        }

        if (!data || !Array.isArray(data) || data.length === 0) {
            targetTbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">You did not attend any program today</td></tr>';
            return;
        }

        let rowsHtml = '';
        data.forEach((item) => {
             if (!Array.isArray(item) || item.length < 3) return;
             const subjectName = item[0] || 'N/A';
             const timetableCode = item[1];
             const feedbackGiven = item[2] === 'Y';
             const existingFeedback = feedbackGiven ? (item[3] || '') : '';

            rowsHtml += `
                <tr>
                    <td>${subjectName}</td>
                    <td>
                        <textarea id="dayfeedback-${timetableCode}"
                                  class="form-control form-control-sm daily-feedback-text"
                                  rows="2"
                                  placeholder="${feedbackGiven ? 'Feedback submitted' : 'Enter feedback...'}"
                                  ${feedbackGiven ? 'readonly' : ''}>${existingFeedback}</textarea>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-primary save-daily-feedback-btn"
                                data-timetablecode="${timetableCode}"
                                ${feedbackGiven ? 'disabled' : ''}>
                            <i class="fa ${feedbackGiven ? 'fa-check' : 'fa-save'}"></i> ${feedbackGiven ? 'Saved' : 'Save'}
                        </button>
                    </td>
                </tr>`;
        });

        targetTbody.innerHTML = rowsHtml;

    } catch (error) {
        showError(targetTbody, `<tr><td colspan="3" class="text-center text-danger py-3">Could not load subjects: ${error.message}</td></tr>`);
    }
};

 // 3. Save Daily Feedback
 const saveDailyFeedback = async (button) => {
     const timetableCode = button.dataset.timetablecode;
     const textarea = document.getElementById(`dayfeedback-${timetableCode}`);
     const feedbackText = textarea ? textarea.value.trim() : '';

     if (!timetableCode || !textarea) {
         alert('Error: Could not find feedback elements.');
         return;
     }
     if (feedbackText === '') {
         alert('Please enter feedback before saving.');
         textarea.focus();
         return;
     }

     button.disabled = true;
     button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';

     try {
         const formData = new URLSearchParams();
         formData.append('programtimetablecode', timetableCode);
         formData.append('feedback', feedbackText);

         const response = await fetch(`/nerie/participant/feedback/save-daily-feedback`, {
             method: 'POST',
             headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
             body: formData.toString()
         });
         const result = await response.text();

         if (response.ok && result === '1') {
             alert('Feedback saved successfully!');
             textarea.readOnly = true;
             button.innerHTML = '<i class="fa fa-check"></i> Saved';
         } else {
             throw new Error(result && result !== '1' ? result : `Failed to save. Server responded with status ${response.status}`);
         }

     } catch (error) {
         alert(`Could not save feedback. ${error.message}`);
         button.disabled = false; // Re-enable button on error
         button.innerHTML = '<i class="fa fa-save"></i> Save';
     }
 };

// 4. Fetch Overall Feedback Data
const fetchOverallFeedback = async (phaseid, formElement) => {
    const feedbackTextarea = formElement.querySelector('#overall_feedback_textarea');
    const submitButton = formElement.querySelector('#submitOverallFeedbackBtn');
    const submittedDiv = formElement.querySelector('#overall_feedback_submitted_div');
    const submittedDateSpan = formElement.querySelector('#overall_feedback_submitted_date');
    const errorMsgDiv = document.getElementById('overallFeedbackErrorMsg');

    // Reset form state
    formElement.reset();
    if(errorMsgDiv) errorMsgDiv.textContent = '';
    if(feedbackTextarea) { feedbackTextarea.readOnly = false; feedbackTextarea.value = ''; }
    if(submitButton) { submitButton.disabled = false; submitButton.style.display = 'inline-block'; submitButton.innerHTML = 'SUBMIT'; }
    if(submittedDiv) submittedDiv.style.display = 'none';
    if(submittedDateSpan) submittedDateSpan.textContent = '';
    const phaseIdInput = formElement.querySelector('#overall_feedback_phaseid');
    if(phaseIdInput) phaseIdInput.value = phaseid;

    try {
         const formData = new URLSearchParams();
         formData.append('phaseid', phaseid);

         const response = await fetch(`/nerie/participant/feedback/overall-feedback`, {
             method: 'POST',
             headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
             body: formData.toString()
         });

        if (!response.ok) {
            let errorText = `HTTP error! status: ${response.status} - ${response.statusText}`;
             try {
                const errorBody = await response.text();
                if(errorBody) { errorText += `\nServer response: ${errorBody}`; }
             } catch(e) { /* Ignore */ }
            throw new Error(errorText);
        }

        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
             throw new Error(`Could not parse server response as JSON. ${jsonError.message}.`);
        }

        if (data && Array.isArray(data) && data.length > 0 && Array.isArray(data[0]) && data[0].length >= 4) {
            const feedbackInfo = data[0];
            const feedbackSlno = feedbackInfo[0];
            const feedbackText = feedbackInfo[2];
            const submittedDate = feedbackInfo[3];

            const slnoInput = formElement.querySelector('#overall_feedback_slno');
            if(slnoInput) slnoInput.value = feedbackSlno || '';
            if(feedbackTextarea) feedbackTextarea.value = feedbackText || '';
            if(feedbackTextarea) feedbackTextarea.readOnly = true;
            if(submittedDateSpan) submittedDateSpan.textContent = formatDate(submittedDate);
            if(submittedDiv) submittedDiv.style.display = 'flex';
            if(submitButton) submitButton.style.display = 'none';
        } else {
             const slnoInput = formElement.querySelector('#overall_feedback_slno');
             if(slnoInput) slnoInput.value = '';
             if(feedbackTextarea) feedbackTextarea.readOnly = false;
             if(submitButton) submitButton.style.display = 'inline-block';
             if(submittedDiv) submittedDiv.style.display = 'none';
        }

    } catch (error) {
        if(errorMsgDiv) errorMsgDiv.textContent = `Could not load existing feedback status: ${error.message}`;
        if(submitButton) submitButton.disabled = true; // Disable submit if loading failed
    }
};

// 5. Save Overall Feedback
const saveOverallFeedback = async (formElement) => {
     const submitButton = formElement.querySelector('#submitOverallFeedbackBtn');
     const errorMsgDiv = document.getElementById('overallFeedbackErrorMsg');
     if(errorMsgDiv) errorMsgDiv.textContent = ''; // Clear previous errors

     const formData = new FormData(formElement);
     const feedbackText = formData.get('feedback');

     if (!window.confirm("Are you sure you want to submit the feedback?")) {
         return;
     }

     if (!feedbackText || feedbackText.trim() === '') {
         if(errorMsgDiv) errorMsgDiv.textContent = 'Feedback cannot be empty.';
         formElement.querySelector('#overall_feedback_textarea')?.focus();
         return;
     }

     const formActionUrl = formElement.getAttribute('action');
     if (!formActionUrl) {
         if(errorMsgDiv) errorMsgDiv.textContent = 'Configuration error: Form action missing.';
         return;
     }

     submitButton.disabled = true;
     submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> SUBMITTING...';

     try {
         const response = await fetch(formActionUrl, {
             method: 'POST',
             body: new URLSearchParams(formData)
         });
         const result = await response.text();

         if (response.ok && result === '2') {
             alert('Successfully Submitted!!!');
             window.location.reload();
             // Or fetchOverallFeedback(formData.get('phaseid'), formElement);
         } else {
             throw new Error(result && result !== '2' ? result : `Failed to submit. Server responded with status ${response.status}`);
         }

     } catch (error) {
         if(errorMsgDiv) errorMsgDiv.textContent = `Submission failed: ${error.message}`;
         alert('Save Failed!!! ' + (error.message || ''));
         submitButton.disabled = false;
         submitButton.innerHTML = 'SUBMIT';
     }
};

// 6. Fetch Attendance Data
 const fetchAttendanceData = async (phaseid, targetDiv) => {
    showLoading(targetDiv);
    try {
         const url = `/nerie/participant/attendance/program-attendance?phaseid=${encodeURIComponent(phaseid)}`;
         const response = await fetch(url);

        if (!response.ok) {
             let errorText = `HTTP error! status: ${response.status} - ${response.statusText}`;
             try {
                 const errorBody = await response.text();
                 if (errorBody) { errorText += `\nServer response: ${errorBody}`; }
             } catch (e) {}
            throw new Error(errorText);
        }

        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
             throw new Error(`Could not parse server response as JSON. ${jsonError.message}.`);
        }

         if (!data || !Array.isArray(data) || data.length === 0) {
            targetDiv.innerHTML = '<p class="text-center text-muted py-3">No attendance records found for this program.</p>';
            return;
        }

         const programInfoRow = data[0];
         let attendanceHtml = '';
         if (Array.isArray(programInfoRow) && programInfoRow.length >= 3) {
             const programStartDate = formatDate(programInfoRow[1]);
             const programEndDate = formatDate(programInfoRow[2]);
             if (programStartDate !== 'N/A' && programEndDate !== 'N/A') {
                attendanceHtml += `<p class="text-center mb-3">Attendance records (Program Dates: ${programStartDate} to ${programEndDate})</p>`;
             }
         }

         attendanceHtml += `<table class="table table-striped table-bordered center-sans-serif fs-12" id="attendanceDisplayTable">
                <thead class="fs-14">
                    <tr>
                        <th>Date</th>
                        <th>Timing</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>`;

        data.forEach((item) => {
             if (!Array.isArray(item) || item.length < 6) return; // Validate row structure
             const attendanceDate = formatDate(item[5]); // Index 5 is entrydate
             const startTime = formatTime(item[3]);      // Index 3 is starttime
             const endTime = formatTime(item[4]);        // Index 4 is endtime
             const status = "Present";

            attendanceHtml += `
                <tr>
                    <td class="text-nowrap">${attendanceDate}</td>
                    <td class="text-nowrap">${startTime} - ${endTime}</td>
                    <td><span class="badge badge-success p-2">${status}</span></td>
                </tr>`;
        });

        attendanceHtml += `</tbody></table>`;
        targetDiv.innerHTML = attendanceHtml;

    } catch (error) {
        showError(targetDiv, `Could not load attendance records: ${error.message}`);
    }
};


// 7. Fetch Timetable Data
const fetchTimetableData = async (phaseid, applicationcode, targetDiv) => {
    showLoading(targetDiv);
    try {
         const url = `/nerie/timetable/program-timetable?phaseid=${encodeURIComponent(phaseid)}&applicationcode=${encodeURIComponent(applicationcode)}`;
         const response = await fetch(url);

        if (!response.ok) {
             let errorText = `HTTP error! status: ${response.status} - ${response.statusText}`;
             try {
                const errorBody = await response.text();
                if(errorBody) { errorText += `\nServer response: ${errorBody}`; }
             } catch(e) {}
            throw new Error(errorText);
        }


        // --- JSON Parsing ---
        let data;
        try {
            data = await response.json();
        } catch (jsonError) {
             throw new Error(`Could not parse server response as JSON. ${jsonError.message}.`);
        }

        if (!data || !Array.isArray(data) || data.length === 0) {
            targetDiv.innerHTML = '<p class="text-center text-muted py-3">No timetable found for this program.</p>';
            return;
        }

        let timetableHtml = `<table class="table table-striped table-bordered center-sans-serif fs-12" id="timetableDisplayTable">
            <thead class="fs-14">
                 <tr>
                     <th>Subject</th>
                     <th>Program Day</th>
                     <th>Resource Person</th>
                     <th>Room No.</th>
                     <th>Date</th>
                     <th>Start Time</th>
                     <th>End Time</th>
                 </tr>
             </thead>
             <tbody>`;

         data.forEach((item) => {
             if (!Array.isArray(item) || item.length < 13) return; // Skip invalid row

             const subject = item[9] || 'N/A';
             const programDay = item[10] || 'N/A';
             const resourcePerson = item[11] || 'N/A';
             const roomNo = item[12] || 'N/A';
             const timetableDate = formatDate(item[7]);
             const startTime = formatTime(item[5]);
             const endTime = formatTime(item[6]);

             timetableHtml += `
                 <tr>
                     <td>${subject}</td>
                     <td>${programDay}</td>
                     <td>${resourcePerson}</td>
                     <td>${roomNo}</td>
                     <td class="text-nowrap">${timetableDate}</td>
                     <td class="text-nowrap">${startTime}</td>
                     <td class="text-nowrap">${endTime}</td>
                 </tr>`;
         });

        timetableHtml += `</tbody></table>`;
        targetDiv.innerHTML = timetableHtml;

    } catch (error) {
        showError(targetDiv, `Could not load the timetable: ${error.message}`);
    }
};


if (myProgramTable) {
    myProgramTable.addEventListener('click', (event) => {
        const target = event.target;

        const materialBtn = target.closest('.download-material-btn');
        const dailyFeedbackBtn = target.closest('.daily-feedback-btn');
        const overallFeedbackBtn = target.closest('.overall-feedback-btn');
        const attendanceBtn = target.closest('.view-attendance-btn');
        const timetableBtn = target.closest('.view-timetable-btn');

        // Use jQuery to trigger modal show for BS4
        const triggerModalShow = (modalId) => {
             if (typeof $ !== 'undefined' && $(modalId).length) {
                  $(modalId).modal('show');
             }
         }

        // 1. Material Button Click
        if (materialBtn) {
            event.preventDefault();
            const phaseid = materialBtn.dataset.phaseid;
            const modalTargetDiv = document.getElementById('materialTableDiv');
            const modalId = '#modalmaterial-modal';
            if (phaseid && modalTargetDiv) {
                 fetchMaterialData(phaseid, modalTargetDiv);
                 triggerModalShow(modalId);
            }
        }

        // 2. Daily Feedback Button Click
        else if (dailyFeedbackBtn) {
            event.preventDefault();
            const programcode = dailyFeedbackBtn.dataset.programcode;
            const phaseid = dailyFeedbackBtn.dataset.phaseid;
            // const programName = dailyFeedbackBtn.dataset.programname || '';
            const modalTargetTbody = document.getElementById('daywiseSubjectsTbody');
            const modalTitle = document.getElementById('daywiseFeedbackModalLabel');
            const modalId = '#daywisefeedbackmodal';
            if (programcode && phaseid && modalTargetTbody) {
                 if(modalTitle) modalTitle.textContent = `Feedback For Today's Sessions`; // Set title
                 fetchDailyFeedbackSubjects(programcode, phaseid, modalTargetTbody);
                 triggerModalShow(modalId);
            }
        }

        // 3. Overall Feedback Button Click
        else if (overallFeedbackBtn) {
             event.preventDefault();
             const phaseid = overallFeedbackBtn.dataset.phaseid;
             const programName = overallFeedbackBtn.dataset.programname || 'N/A';
             const modalForm = document.getElementById('overallFeedbackForm');
             const programNameDisplay = document.getElementById('overall_feedback_program_name_display');
             const modalId = '#modalfeed-modal';

             if (phaseid && modalForm && programNameDisplay) {
                  programNameDisplay.textContent = programName;
                  fetchOverallFeedback(phaseid, modalForm);
                  triggerModalShow(modalId);
             }
        }

        // 4. Attendance Button Click
        else if (attendanceBtn) {
            event.preventDefault();
            const phaseid = attendanceBtn.dataset.phaseid;
            const programName = attendanceBtn.dataset.programname || 'N/A';
            const modalTargetDiv = document.getElementById('attendanceTableDiv');
            const programNameHeading = document.getElementById('attendanceProgramName');
            const modalId = '#modalattendance-modal';
             if (phaseid && modalTargetDiv && programNameHeading) {
                programNameHeading.textContent = `Program: ${programName}`;
                fetchAttendanceData(phaseid, modalTargetDiv);
                triggerModalShow(modalId);
             }
        }

         // 5. Timetable Button Click
         else if (timetableBtn) {
             event.preventDefault();
             const phaseid = timetableBtn.dataset.phaseid;
             const applicationcode = timetableBtn.dataset.applicationcode;
             const programName = timetableBtn.dataset.programname || 'N/A';
             const modalTargetDiv = document.getElementById('timetableTableDiv');
             const programNameHeading = document.getElementById('timetableProgramName');
             const modalId = '#modaltimetable-modal';
             if (phaseid && applicationcode && modalTargetDiv && programNameHeading) {
                 programNameHeading.textContent = `Program: ${programName}`;
                 fetchTimetableData(phaseid, applicationcode, modalTargetDiv);
                 triggerModalShow(modalId);
             }
         }
    });
}


// --- Daily Feedback Save Buttons ---
const daywiseModalBody = document.querySelector('#daywisefeedbackmodal .modal-body');
if (daywiseModalBody) {
    daywiseModalBody.addEventListener('click', (event) => {
        const saveButton = event.target.closest('.save-daily-feedback-btn');
        if (saveButton && !saveButton.disabled) {
             event.preventDefault();
             saveDailyFeedback(saveButton);
        }
    });
}

// --- Overall Feedback Form Submission ---
const overallFeedbackForm = document.getElementById('overallFeedbackForm');
if (overallFeedbackForm) {
    // if (!overallFeedbackForm.action) {
    //     overallFeedbackForm.action = '/nerie/feedback/save-overall-feedback';
    // }

    overallFeedbackForm.addEventListener('submit', (event) => {
        event.preventDefault();
         saveOverallFeedback(overallFeedbackForm);
    });

    // reset/cancel buttons inside the overall feedback modal
    document.querySelectorAll('#modalfeed-modal .reset-overall-feedback-form').forEach(button => {
        button.addEventListener('click', () => {
             const errorMsgDiv = document.getElementById('overallFeedbackErrorMsg');
             if (errorMsgDiv) errorMsgDiv.textContent = '';
             overallFeedbackForm.reset();

             const submitButton = overallFeedbackForm.querySelector('#submitOverallFeedbackBtn');
             const feedbackTextarea = overallFeedbackForm.querySelector('#overall_feedback_textarea');
             const submittedDiv = overallFeedbackForm.querySelector('#overall_feedback_submitted_div');
             const programNameDisplay = document.getElementById('overall_feedback_program_name_display');
             if(feedbackTextarea) feedbackTextarea.readOnly = false;
             if(submitButton) {
                 submitButton.disabled = false;
                 submitButton.style.display = 'inline-block';
                 submitButton.innerHTML = 'SUBMIT';
             }
             if(submittedDiv) submittedDiv.style.display = 'none';
             if(programNameDisplay) programNameDisplay.textContent = '';
        });
    });

}

// --- DataTables Initialization ---
if (typeof $ !== 'undefined' && $.fn.DataTable) {
    if ($('#myProgramsTable').length) {
        try {
            $('#myProgramsTable').DataTable({
                dom: 'Blfrtip', // Note the 'B' added here
                pageLength: 5,
                lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
                buttons: [
                    {
                        extend: 'excelHtml5',
                        text: '<i class="fa fa-file-excel-o"></i> Excel',
                        title: 'Programs',
                        exportOptions: {
                            columns: ':visible:not(.noExport)'
                        },
                        className: 'btn btn-sm btn-outline-success'
                    }
                ]
            });
        } catch (e) {
            console.error("DataTable initialization error:", e);
        }
    }
}


 // Modal Reset on Hide
 if (typeof $ !== 'undefined' && $.fn.modal) {
      $('.modal').on('hidden.bs.modal', function (event) {
         const modalElement = event.target;

         const contentAreas = [
             modalElement.querySelector('#materialTableDiv'),
             modalElement.querySelector('#daywiseSubjectsTbody'),
             modalElement.querySelector('#attendanceTableDiv'),
             modalElement.querySelector('#timetableTableDiv')
         ];
         contentAreas.forEach(area => {
             if (area) {
                 area.innerHTML = '';
             }
         });

         // Reset overall feedback form
         if (modalElement.id === 'modalfeed-modal') {
             const overallForm = modalElement.querySelector('#overallFeedbackForm');
             if (overallForm) {
                 const errorMsgDiv = document.getElementById('overallFeedbackErrorMsg');
                 if (errorMsgDiv) errorMsgDiv.textContent = '';
                 overallForm.reset();

                 const submitButton = overallForm.querySelector('#submitOverallFeedbackBtn');
                 const feedbackTextarea = overallForm.querySelector('#overall_feedback_textarea');
                 const submittedDiv = overallForm.querySelector('#overall_feedback_submitted_div');
                 const programNameDisplay = document.getElementById('overall_feedback_program_name_display');
                 if(feedbackTextarea) feedbackTextarea.readOnly = false;
                 if(submitButton) {
                     submitButton.disabled = false;
                     submitButton.style.display = 'inline-block';
                     submitButton.innerHTML = 'SUBMIT';
                 }
                 if(submittedDiv) submittedDiv.style.display = 'none';
                 if(programNameDisplay) programNameDisplay.textContent = '';
             }
         }

          const modalDataTableSelectors = ['#materialDataTable'];
          modalDataTableSelectors.forEach(selector => {
              const table = modalElement.querySelector(selector);

              if (table && $.fn.DataTable.isDataTable(table)) {
                   try {
                        $(table).DataTable().destroy();
                   } catch (e) {}
              }
          });
     });
 }

});