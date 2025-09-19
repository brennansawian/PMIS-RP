var sTable;

const saveCourseUrl = '/nerie/course-academics/saveMapDepartmentCourse';
const updateCourseUrl = '/nerie/course-academics/updateDepartmentCourse';
const getCoursesUrl = '/nerie/course-academics/getListOfCourses';

function initializeDataTable() {
    return $('#c-table').DataTable({
        dom: 'Blfrtip',
        pageLength: 10,
        lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Academic Courses',
                exportOptions: { columns: ':visible:not(.noExport)' },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });
}

$(document).ready(function () {
    // Input validation helpers
    $('input.alphabets').keyup(function () {
        this.value = this.value.replace(/[^a-zA-Z. ]/g, '');
    });
    $('input.onlyletters').keyup(function () {
        this.value = this.value.replace(/[^a-zA-Z]/g, '');
    });
    $('input.coursename').keyup(function () {
        this.value = this.value.replace(/[^a-zA-Z0-9\)(\&. ]/g, '');
    });
    $('input.courseduration').keyup(function () {
        this.value = this.value.replace(/[^0-9]/g, '');
    });
    $("#courseid, #edit_courseid").keyup(function () {
        $(this).val($(this).val().toUpperCase());
    });

    sTable = initializeDataTable();

    $('#departmentcode').on('change', function () {
        loadCourses();
    });

    $('#clearCourseBtn').on('click', function() {
        clearCourse();
    });

    $('#newcourseform').submit(function (e) {
        e.preventDefault();
        callCustomAjax(saveCourseUrl, $(this).serialize(), function (data) {
            if (data === "2") {
                showModalAlert('Successfully Saved!','Message')
                //Notiflix.Notify.Success('Successfully Saved!');
                clearCourse();
                loadCourses();
                $('#course-modal').modal('hide');
            } else if (data === "3") {
                Notiflix.Report.Failure('Save Failed', 'Course Name Already Exists!', 'Okay');
            } else if (data === "4") {
                Notiflix.Report.Failure('Save Failed', "Course ID already exists!", 'Okay');
            } else {
                Notiflix.Report.Failure('Save Failed', 'Failed to save course. Please try again.', 'Okay');
            }
        });
    });

    $('#editcourseform').submit(function (e) {
        e.preventDefault();
        callCustomAjax(updateCourseUrl, $(this).serialize(), function (data) {
            if (data === "2") {
                showModalAlert('Successfully Updated!','Message')
                //Notiflix.Notify.Success('Successfully Updated!');
                loadCourses();
                $('#edit-course-modal').modal('hide');
            } else if (data === "3") {
                Notiflix.Report.Failure('Update Failed', 'Course Name Already Exists!', 'Okay');
            } else if (data === "4") {
                Notiflix.Report.Failure('Update Failed', "Course ID already exists!", 'Okay');
            } else if(data === "5") {
                Notiflix.Report.Failure('Update Failed', "Course not found", 'Okay');
            } else {
                Notiflix.Report.Failure('Update Failed', 'Failed to update course. Please try again.', 'Okay');
            }
        });
    });

    $('#c-table tbody').on('click', '.editCourseBtn', function() {
        var btn = $(this);
        editCourse(
            btn.data('ccode'),
            btn.data('cname'),
            btn.data('dcode'),
            btn.data('cid'),
            String(btn.data('ist')),
            btn.data('duration')
        );
    });

});

function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

function clearCourse() {
    $('#newcourseform')[0].reset();
    $('#coursecode').val('');
    $('#departmentcode2').val('');
    // Reset radio buttons to "No" for add form
    $('#isshortterm_no_add').prop('checked', true).parent().addClass('active');
    $('#isshortterm_yes_add').prop('checked', false).parent().removeClass('active');
}

function loadCourses() {
    var departmentCode = $('#departmentcode').val();
    if (!departmentCode) {
        sTable.clear().draw();
        return;
    }

    callCustomAjax(getCoursesUrl, { departmentcode: departmentCode }, function (data) {
        if (sTable) {
            sTable.clear().destroy(); // Destroy the old instance
        }
        $('#c-table tbody').empty(); // Clear the table body

        if (data && data.length > 0) {
            var count = 1;
            data.forEach(function (x) {
                var courseType = (String(x[5]) === '1') ? "Short Term" : "Long Term";
                var editButton = `<button class='btn btn-sm btn-info editCourseBtn'
                    data-ccode='${x[0]}'
                    data-cname='${x[1]}'
                    data-dcode='${x[3]}'
                    data-cid='${x[4]}'
                    data-ist='${x[5]}'
                    data-duration='${x[6]}'>Edit</button>`;

                var row = `<tr>
                    <td>${count++}</td>
                    <td>${x[1]}</td>
                    <td>${x[2]}</td>
                    <td>${x[6]} Years</td>
                    <td>${courseType}</td>
                    <td>${editButton}</td>
                </tr>`;
                $('#c-table tbody').append(row);
            });
        }

        sTable = initializeDataTable(); // Re-initialize DataTable with new data
    });
}

function editCourse(ccode, cname, dcode, cid, ist, duration) {
    $('#edit_coursecode').val(ccode);
    $('#edit_coursename').val(cname);
    $('#edit_departmentcode').val(dcode);
    $('#edit_courseid').val(cid);
    $('#edit_courseduration').val(duration);

    if (ist === '1') {
        $('#isshortterm_yes_edit').prop('checked', true).parent().addClass('active');
        $('#isshortterm_no_edit').prop('checked', false).parent().removeClass('active');
    } else {
        $('#isshortterm_no_edit').prop('checked', true).parent().addClass('active');
        $('#isshortterm_yes_edit').prop('checked', false).parent().removeClass('active');
    }
    $('#edit-course-modal').modal('show');
}

function callCustomAjax(url, data, successCallback) {
    $.ajax({
        type: 'POST',
        url: url,
        data: data,
        success: successCallback,
        error: function(jqXHR, textStatus, errorThrown) {
            Notiflix.Report.Failure('AJAX Error', `${textStatus} - ${errorThrown}`, 'Okay');
        }
    });
}