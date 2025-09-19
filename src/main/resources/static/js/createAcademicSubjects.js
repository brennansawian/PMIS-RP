var sTable;

$(document).ready(function () {
    $('#isshortterm_toggle').on('change', function() {
        if ($(this).is(':checked')) {
            $('#isshortterm').val('1'); // Set hidden input value to 1
            $('#isshortterm_label').text('Yes'); // Change text to Yes
        } else {
            $('#isshortterm').val('0'); // Set hidden input value to 0
            $('#isshortterm_label').text('No'); // Change text to No
        }

        $('#isshortterm').trigger('change');
    });

    sTable = $('#s-table').DataTable({
        dom: 'Blfrtip',
        pageLength: 10,
        lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Academic Subjects',
                exportOptions: { columns: ':visible:not(.noExport)' },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('input.alphabets').keyup(function () {
        if (this.value.match(/[^a-zA-Z. ]/g)) {
            this.value = this.value.replace(/[^a-zA-Z. ]/g, '');
        }
    });

    $('#s-table tbody').on('click', 'button.edit-subject-btn', function () {
        const button = $(this);
        editSubject(
            button.data('subjectname'), button.data('subjectcode'),
            button.data('departmentcode'), button.data('semestercode'),
            button.data('sphase'), button.data('isshortterm'),
            button.data('coursecode'), button.data('isoptional')
        );
    });

    $('#isshortterm_toggle').on('change', function() {
        toggleViz();
        if ($('#departmentcode').val()) {
             getdepartments($('#departmentcode').val(), $('#isshortterm').val());
        }
        loadSubjects();
    });

    $('#departmentcode, #coursecode, #semestercode, #sphase').on('change', function() {
        loadSubjects();
    });

    $('#newsubjectform').submit(function (e) {
        e.preventDefault();
        if ($('#isshortterm').val() === '0' && !$('#semestercode').val()) {
            Notiflix.Report.Warning('Validation Failed', 'Please select a semester.', 'Okay');
            return;
        }
        if ($('#isshortterm').val() === '1' && !$('#sphase').val()) {
            Notiflix.Report.Warning('Validation Failed', 'Please select a phase.', 'Okay');
            return;
        }

        callCustomAjax('/nerie/subjects/saveNewSubject', $('#newsubjectform').serialize(), function (data) {
            if (data === "2") {
                showModalAlert('Successfully Saved!', 'Message');
                //$('#newsubjectform')[0].reset();
                //$('#departmentcode').val('');
                //$('#coursecode').empty().append($('<option value="">Select Course</option>'));
                //$('#isshortterm_toggle').prop('checked', false);
                //toggleViz();
                loadSubjects();
            } else if (data === "3") {
                Notiflix.Report.Failure('Save Failed', 'Subject-Department Mapping already exists!', 'Okay');
            } else {
                Notiflix.Report.Failure('Save Failed', 'An unknown error occurred while saving the subject.', 'Okay');
            }
        });
    });

    $('#departmentcode').change(function () {
        if ($(this).val()) {
            getdepartments($(this).val(), $('#isshortterm').val());
        } else {
            $('#coursecode').empty().append('<option value="">Select Course</option>');
        }
        loadSubjects();
    });

    loadSubjects();

    const departmentModal = document.getElementById('department-modal');
    const departmentForm = document.getElementById('departmentForm');
    const departmentNameInput = document.getElementById('departmentname_modal');
    const addDepartmentBtn = document.getElementById('adddept');

    const resetDepartmentForm = () => {
        departmentForm.reset();
        departmentForm.classList.remove('was-validated');
    };

    addDepartmentBtn.addEventListener('click', resetDepartmentForm);

    departmentForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!departmentForm.checkValidity()) {
            event.stopPropagation();
            departmentForm.classList.add('was-validated');
            return;
        }
        departmentForm.classList.add('was-validated');

        const submitButton = document.getElementById('submitDepartmentForm');
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';

        try {
            const response = await fetch(departmentForm.action, {
                method: 'POST',
                body: new URLSearchParams(new FormData(departmentForm))
            });

            const responseBody = await response.text();

            if (!response.ok) {
                 throw new Error(`Server error! Status: ${response.status}`);
            }

            switch (responseBody.trim()) {
                case "1":
                    Notiflix.Report.Failure('Save Failed', 'Department Name Already Exists!', 'Okay');
                    departmentNameInput.focus();
                    departmentNameInput.value = "";
                    departmentForm.classList.remove('was-validated');
                    break;
                case "2":
                    showModalAlert('Successfully Saved!', 'Message');
                    $(departmentModal).modal('hide');
                    window.location.reload();
                    break;
                default:
                    Notiflix.Report.Failure('Save Failed', 'Please check the details and try again.', 'Okay');
                    break;
            }
        } catch (error) {
            Notiflix.Report.Failure('Error', `An error occurred: ${error.message}`, 'Okay');
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'SUBMIT';
        }
    });

    $(departmentModal).on('hidden.bs.modal', resetDepartmentForm);
});

function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

function loadSubjects() {
    var deptCode = $('#departmentcode').val();
    var courseCode = $('#coursecode').val();
    var semCode = $('#semestercode').val();
    var sphaseVal = $('#sphase').val();
    var isShortTerm = $('#isshortterm').val();

    if (!deptCode || !courseCode || (isShortTerm === '0' && !semCode) || (isShortTerm === '1' && !sphaseVal)) {
        if(sTable) sTable.clear().draw();
        return;
    }

    $.ajax({
        type: "POST",
        url: "/nerie/subjects/getListOfSubjects",
        data: `departmentcode=${deptCode}&semestercode=${isShortTerm === '0' ? semCode : ""}&sphase=${isShortTerm === '1' ? sphaseVal : ""}&coursecode=${courseCode}&isshortterm=${isShortTerm}`,
        success: function (data) {
            sTable.clear();
            if (data && data.length > 0) {
                let tableData = [];
                data.forEach((x, index) => {
                    const buttonHtml = `<button class='btn btn-sm btn-info edit-subject-btn'
                        data-subjectname="${x[1]}" data-subjectcode="${x[0]}"
                        data-departmentcode="${x[4]}" data-semestercode="${x[5]}"
                        data-sphase="${x[7]}" data-isshortterm="${x[8]}"
                        data-coursecode="${x[9]}" data-isoptional="${x[11]}">Edit</button>`;

                    tableData.push([
                        index + 1, x[1], x[2], x[10], (x[8] === '1' ? x[6] : x[3]), buttonHtml
                    ]);
                });
                sTable.rows.add(tableData);
            }
            sTable.draw();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            Notiflix.Report.Failure('Load Error', `Failed to load subjects: ${textStatus} - ${errorThrown}`, 'Okay');
            sTable.clear().draw();
        }
    });
}

function getdepartments(departmentcode, isshortterm) {
    callCustomAjaxasync("/nerie/course-academics/getCoursesBasedOnDepartment", `departmentcode=${departmentcode}&isshortterm=${isshortterm}`, function (data) {
        $('#coursecode').empty().append('<option value="">Select Course</option>');
        data.forEach(function (item) {
            $('#coursecode').append(`<option data-duration="${item[5]}" value="${item[0]}">${item[1]}</option>`);
        });
        loadSubjects();
    });
}

function editSubject(sname, subcode, dcode, scode, sphase, ist, ccode, isopt) {
    $("#subjectcode").val(subcode);
    $("#departmentcode").val(dcode);
    $("#isshortterm_toggle").prop('checked', ist === '1');
    toggleViz();

    callCustomAjaxasync("/nerie/course-academics/getCoursesBasedOnDepartment", `departmentcode=${dcode}&isshortterm=${ist}`, function (data) {
        $('#coursecode').empty().append('<option value="">Select Course</option>');
        data.forEach(function (item) {
            $('#coursecode').append(`<option data-duration="${item[5]}" value="${item[0]}">${item[1]}</option>`);
        });

        $("#coursecode").val(ccode);
        if (ist === '1') { $("#sphase").val(sphase); } else { $("#semestercode").val(scode); }
        $("#subjectname").val(sname);
        $("input[name=isoptional][value=" + isopt + "]").prop('checked', true);
        $('html, body').animate({ scrollTop: 0 }, 'fast');
    });
}

function toggleViz() {
    if ($("#isshortterm_toggle").is(":checked")) {
        $('#semesterdiv').hide();
        $('#phasediv').show();
        $('#sphase').prop('required', true);
        $('#semestercode').prop('required', false).val('');
        $("#isshortterm_label").html("Yes");
        $("#isshortterm").val("1");
    } else {
        $('#semesterdiv').show();
        $('#phasediv').hide();
        $('#sphase').prop('required', false).val('');
        $('#semestercode').prop('required', true);
        $("#isshortterm_label").html("No");
        $("#isshortterm").val("0");
    }
    $('#coursecode').empty().append('<option value="">Select Course</option>');
}

function callCustomAjax(url, data, successCallback) {
    $.ajax({
        type: 'POST', url: url, data: data, success: successCallback,
        error: function(jqXHR, textStatus, errorThrown) {
            Notiflix.Report.Failure('AJAX Error', `${textStatus} - ${errorThrown}`, 'Okay');
        }
    });
}

function callCustomAjaxasync(url, data, successCallback) {
    $.ajax({
        type: 'POST', url: url, data: data, success: successCallback,
        error: function(jqXHR, textStatus, errorThrown) {
             Notiflix.Report.Failure('AJAX Error', `${textStatus} - ${errorThrown}`, 'Okay');
        }
    });
}