$(document).ready(function () {

    const createOrUpdateFacultyUrl ='/nerie/faculties/createEditFaculty';
    const getCoursesUrl =  '/nerie/course-academics/getCoursesBasedOnDepartmentFaculty';
    const facultyDetailsUrlBase = '/nerie/faculties/facultyDetails';
    const facultySubjectsUrlBase = '/nerie/faculties/facultySubjects';

    $('.select2').select2({
         width: '100%'
    });

    $("#userloginfid").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: createOrUpdateFacultyUrl,
            data: $("#userloginfid").serialize(),
            success: function (data) {
                if (data === "1" || data === 1) {
                    showModalAlert('Faculty successfully saved.', 'Message');

                    $('#feedbackModal').one('hidden.bs.modal', function () {
                        location.reload();
                    });

                } else {
                    Notiflix.Notify.Failure("Error saving faculty details: " + data);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                Notiflix.Notify.Failure("AJAX error: " + textStatus + " - " + errorThrown);
            }
        });
    });

    $("#usercode").change(function () {
        const selectedUserCode = $(this).val();
        if (selectedUserCode && selectedUserCode !== '-1') {
             editfaculty(selectedUserCode);
        } else {
            cleardata();
        }
    });

    $('#departmentcode').change(function () {
        const departmentCode = $(this).val();

        if (departmentCode && departmentCode !== '-1') {
            $('#coursecode').empty().append($('<option></option>').attr('value', '-1').text('Loading...')).val('-1').trigger('change');
             $.ajax({
                type: "GET",
                url: getCoursesUrl,
                data: { departmentcode: departmentCode },
                success: function (data) {
                    $('#coursecode').empty();
                    if (data && data.length > 0) {
                        data.forEach(function (item) {
                            $('#coursecode').append($('<option></option>').attr('value', item[0]).text(item[1]));
                        });
                        $('#coursecode').val(null).trigger('change');
                    } else {
                         $('#coursecode').append($('<option></option>').attr('value', '-1').text('No courses found')).val('-1').trigger('change');
                    }
                },
                error: function() {
                    $('#coursecode').empty().append($('<option></option>').attr('value', '-1').text('Error loading courses')).val('-1').trigger('change');
                }
            });
        } else {
             $('#coursecode').empty();
             $('#coursecode').append($('<option></option>').attr('value', '-1').text('Select'));
             $('#coursecode').val(null).trigger('change');
        }
    });

    $('#usertable').DataTable({
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

    $(document).on('click', '.edit-faculty-btn', function() {
        const facultyUserCode = $(this).data('id');
        if (facultyUserCode) {
            $("#usercode").val(facultyUserCode).trigger('change');
        }
    });

    $('#userloginfid').on('reset', function() {
        setTimeout(function() {
            $('#usercode').val('-1').trigger('change');
        }, 0);
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

function cleardata() {
    $("#facultyid").val('');
    $("#fname").val('');
    $("#mname").val('');
    $("#lname").val('');
    $("#designationcode").val('-1').trigger('change');
    $("#departmentcode").val('-1').trigger('change');
    $('#subject').val(null).trigger('change');
}

function editfaculty(usercode) {
    $('html, body').animate({ scrollTop: 0 }, 'fast');

    const facultyDetailsUrl = '/nerie/faculties/facultyDetails';
    const facultySubjectsUrl = '/nerie/faculties/facultySubjects';
    const getCoursesUrlForEdit = '/nerie/course-academics/getCoursesBasedOnDepartmentFaculty';

    $("#facultyid").val('');
    $("#fname").val('');
    $("#mname").val('');
    $("#lname").val('');
    $("#designationcode").val('-1');
    $("#departmentcode").val('-1');
    $('#subject').val(null).trigger('change');
    $('#coursecode').empty().append($('<option></option>').attr('value', '-1').text('Select')).val(null).trigger('change');


    $.ajax({
        type: "GET",
        url: facultyDetailsUrl,
        data: { usercode: usercode },
        success: function (facultyDataArray) {
            if (facultyDataArray && facultyDataArray.length > 0) {
                const faculty = facultyDataArray[0];

                $("#facultyid").val(faculty[1]);
                $("#fname").val(faculty[2]);
                $("#mname").val(faculty[3]);
                $("#lname").val(faculty[4]);
                $("#designationcode").val(faculty[5]).trigger('change');

                const departmentCodeToSet = faculty[7];
                let selectedCourseCodes = [];
                if (faculty[9]) {
                    if (Array.isArray(faculty[9])) {
                        selectedCourseCodes = faculty[9].map(String);
                    } else {
                        selectedCourseCodes = String(faculty[9]).split(',').map(s => s.trim()).filter(s => s);
                    }
                }

                $("#departmentcode").val(departmentCodeToSet).trigger('change');

                if (departmentCodeToSet && departmentCodeToSet !== '-1') {
                    $.ajax({
                        type: "GET",
                        url: getCoursesUrlForEdit,
                        data: { departmentcode: departmentCodeToSet },
                        success: function(courseOptions) {
                            $('#coursecode').empty();
                            if (courseOptions && courseOptions.length > 0) {
                                courseOptions.forEach(function (item) {
                                    $('#coursecode').append($('<option></option>').val(item[0]).text(item[1]));
                                });
                            } else {
                                $('#coursecode').append($('<option></option>').val('-1').text('No courses found'));
                            }

                            if (selectedCourseCodes.length > 0) {
                                $('#coursecode').val(selectedCourseCodes).trigger('change');
                            } else {
                                $('#coursecode').val(null).trigger('change');
                            }
                        },
                        error: function() {
                            $('#coursecode').empty().append($('<option></option>').val('-1').text('Error loading courses')).val('-1').trigger('change');
                        }
                    });
                }
            } else {
                Notiflix.Notify.Info('No faculty details found for this user. Kindly enter new faculty details.');
                $("#departmentcode").val('-1').trigger('change');
                $('#subject').val(null).trigger('change');
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            Notiflix.Notify.Failure("Error fetching faculty details: " + textStatus + " - " + errorThrown);
            $("#departmentcode").val('-1').trigger('change');
            $('#subject').val(null).trigger('change');
        }
    });

    $.ajax({
        type: "GET",
        url: facultySubjectsUrl,
        data: { usercode: usercode },
        success: function (subjectData) {
            if (subjectData && subjectData.length > 0) {
                var subjectCodes = subjectData.map(function (item) { return String(item[0]); });
                $('#subject').val(subjectCodes).trigger('change');
            } else {
                $('#subject').val(null).trigger('change');
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
             Notiflix.Notify.Failure("Error fetching faculty subjects: " + textStatus + " - " + errorThrown);
             $('#subject').val(null).trigger('change');
        }
    });
}