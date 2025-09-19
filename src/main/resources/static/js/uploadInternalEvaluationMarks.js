var iatable;

$(document).ready(function () {
    iatable = $('#internalevaluationtable').DataTable({
        dom: 'Blfrtip',
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Student Feedbacks',
                className: 'btn btn-sm btn-outline-success',
                exportOptions: {
                    columns: ':visible'
                }
            },
            {
                extend: 'pdfHtml5',
                text: '<i class="fa fa-file-pdf-o"></i> PDF',
                title: 'Student Feedbacks',
                className: 'btn btn-sm btn-outline-danger',
                exportOptions: {
                    columns: ':visible'
                }
            }
        ],
        pageLength: 10,
        lengthMenu: [[10, 20, 50, 100, -1], [10, 20, 50, 100, "All"]],
        language: {
            emptyTable: "No students loaded yet. Select subject and test, then click Load Students."
        },
        "columnDefs": [
            { "width": "150px", "targets": 2 } // Set the third column width
        ]
    });

    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });

    $('.sub-menu ul').hide();
    $(".sub-menu a").click(function () {
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $("#marksform").submit(function (e) {
        e.preventDefault();

        if ($("#subjectcode").val() === '-1') {
            Notiflix.Notify.Failure("Please select Subject");
            $("#subjectcode").focus();
            return false;
        }
        if ($("#testid").val() === '-1') {
            Notiflix.Notify.Failure("Please select Test");
            $("#testid").focus();
            return false;
        }

        var formData = new FormData($(this)[0]);

        Notiflix.Loading.Standard('Saving marks...');
        $.ajax({
            type: "POST",
            url: "/nerie/internal-evaluation-marks/saveInternalEvaluation",
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                Notiflix.Loading.Remove();
                if (data === '-1' || data === -1) {
                    showModalAlert("Unable to save. Please try again.");
                } else {
                    showModalAlert("Successfully saved.", "Message");
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                Notiflix.Loading.Remove();
                Notiflix.Notify.Failure("Error saving: " + textStatus + " - " + errorThrown);
            }
        });
    });

    $(".loadstudentsbtn").on("click", function(event) {
        event.preventDefault();
        loadStudents();
    });
});


function loadStudents() {
    const subjectCodeVal = $('#subjectcode').val();
    const testIdVal = $('#testid').val();

    if (subjectCodeVal == "-1") {
        Notiflix.Notify.Info("Select a subject");
        return false;
    }
    if (testIdVal == "-1") {
        Notiflix.Notify.Info("Select a test");
        return false;
    }

    const subjectText = $("#subjectcode option:selected").text();
    const testText = $("#testid option:selected").text();
    const dynamicTitle = `Internal Evaluation for ${subjectText} for ${testText}`;
    $("#tabletitle").html(`<i class="fa fa-list"></i> ${dynamicTitle}`);

    Notiflix.Loading.Standard('Loading students...');
    $.ajax({
        type: "GET",
        url: "/nerie/students/getStudentsList",
        data: { subjectcode: subjectCodeVal, testid: testIdVal },
        success: function (data) {
            Notiflix.Loading.Remove();

            if (iatable) {
                iatable.destroy();
            }
            $('#studentslist').empty();

            $('#marksform input[name="subjectcode"]').remove();
            $('#marksform input[name="testid"]').remove();
            $('#marksform').append(`<input type="hidden" name="subjectcode" value="${subjectCodeVal}" />`);
            $('#marksform').append(`<input type="hidden" name="testid" value="${testIdVal}" />`);

            if (!data || data.length === 0) {
                Notiflix.Notify.Warning("No Students Available for this Semester - Subject combination.");
            } else {
                let studentsHtml = '';
                data.forEach(student => {
                    const studentName = `${student[1] || ''} ${student[2] || ''} ${student[3] || ''}`.trim();
                    const marks = student[11] !== null ? student[11] : '';
                    const internalEvalId = student[9] !== null ? student[9] : '';
                    studentsHtml += `<tr data-studentid='${student[0]}'>
                          <td>${student[0]}</td>
                          <td>${studentName}</td>
                          <td>
                              <input type='number' min='0' name='studentmarks' class='form-control studentmarkinput' value='${marks}' required />
                              <input type='hidden' value='${student[0]}' name='studentids' />
                              <input type='hidden' value='${internalEvalId}' name='internalevaluationids'>
                          </td>
                       </tr>`;
                });
                $('#studentslist').html(studentsHtml);
            }

            iatable = $('#internalevaluationtable').DataTable({
                dom: 'Blfrtip',
                buttons: [
                    {
                        extend: 'excelHtml5',
                        text: '<i class="fa fa-file-excel-o"></i> Excel',
                        title: 'Student Feedbacks',
                        className: 'btn btn-sm btn-outline-success',
                        exportOptions: {
                            columns: ':visible'
                        }
                    },
                    {
                        extend: 'pdfHtml5',
                        text: '<i class="fa fa-file-pdf-o"></i> PDF',
                        title: 'Student Feedbacks',
                        className: 'btn btn-sm btn-outline-danger',
                        exportOptions: {
                            columns: ':visible'
                        }
                    }
                ],
                pageLength: 10,
                lengthMenu: [[10, 20, 50, 100, -1], [10, 20, 50, 100, "All"]],
                language: { emptyTable: "No Students Available." },
                "columnDefs": [
                    { "width": "150px", "targets": 2 } // Set the third column width
                ]
            });
        },
        error: function (jqXHR, textStatus, errorThrown) {
            Notiflix.Loading.Remove();
            Notiflix.Notify.Failure("Error loading students: " + textStatus + " - " + errorThrown);
        }
    });
}

function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}
