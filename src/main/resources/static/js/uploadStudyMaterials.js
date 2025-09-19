$(document).ready(function () {
    $('#assignmentslist').DataTable({
        dom: 'lfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Study Materials',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
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

    $('#stmattable').on('click', '.edit-study-material', function(event) {
        event.preventDefault();
        const studyMaterialId = $(this).data('id');
        const title = $(this).data('title');
        const subject = $(this).data('subject');
        editstudymaterial(studyMaterialId, title, subject);
    });

    $("#subject").change(function () {
        var sub = $("#subject").val();
        var subname = $("#subject option:selected").text();
        Notiflix.Loading.Standard('Fetching materials...');

        $.ajax({
            type: "GET",
            url: "getStudyMaterialsListSubject",
            data: { subjectcode: sub },
            success: function (data) {
                Notiflix.Loading.Remove();
                $("#stmattable").html("");

                if (sub === '-1' || sub === "") {
                    $("#titletext").html("All");
                } else {
                    $("#titletext").html(subname);
                }

                var temp = "";
                if (data && data.length > 0) {
                    for (var i = 0; i < data.length; i++) {
                        var uploadDate = new Date(data[i][2]);
                        var formattedDate = ('0' + uploadDate.getDate()).slice(-2) + '-' +
                                          uploadDate.toLocaleString('default', { month: 'short' }) + '-' +
                                          uploadDate.getFullYear() + ' ' +
                                          ('0' + uploadDate.getHours()).slice(-2) + ':' +
                                          ('0' + uploadDate.getMinutes()).slice(-2) + ':' +
                                          ('0' + uploadDate.getSeconds()).slice(-2);

                        temp += `<tr>
                                <td>${data[i][0]}</td> <!-- title -->
                                <td><a href="viewStudyMaterialDocument?sid=${data[i][1]}" class="btn btn-sm btn-secondary" target="_blank"><i class="fa"></i> View Study Material</a></td>
                                <td>${formattedDate}</td>
                                <td>
                                    <button class="clickme danger11 edit-study-material btn btn-sm btn-info"
                                            data-id="${data[i][1]}"
                                            data-title="${data[i][0]}"
                                            data-subject="${data[i][3]}">
                                        <i class="fa fa-edit"></i> Edit
                                    </button>
                                </td>
                            </tr>`;
                    }
                } else {
                    temp = "<tr><td colspan='4'>No study materials found for this subject.</td></tr>";
                }
                $("#stmattable").html(temp);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                Notiflix.Loading.Remove();
                Notiflix.Report.Failure('Error', "AJAX error: " + textStatus + " - exception: " + errorThrown, 'Ok');
            }
        });
    });
});

/**
 * Populates the edit form with study material data and scrolls to the top of the page.
 */
function editstudymaterial(studymaterialid, title, subject) {
    $("#subject").val((subject || '').toString());
    $("#assignmentName").val((title || '').toString());
    $("#studymaterialid").val((studymaterialid || '').toString());

    $("#subject").focus();
    $("#filemessage").text("If you re-upload document, existing one will be overridden.").addClass("red");

    $('html, body').animate({ scrollTop: 0 }, 'fast');
}

$('#studymaterials').on('submit', function(event) {
    event.preventDefault();

    Notiflix.Loading.Standard('Uploading...');

    var formData = new FormData(this);

    $.ajax({
        type: "POST",
        url: $(this).attr('action'),
        data: formData,
        processData: false,
        contentType: false,

        success: function(response) {
            Notiflix.Loading.Remove();

            if (response === "1") {
                $('#uploadSuccessModal').modal('show');
            } else {
                Notiflix.Report.Failure('Upload Failed', 'There was an error processing your request. Please try again.', 'Ok');
            }
        },

        error: function(jqXHR, textStatus, errorThrown) {
            Notiflix.Loading.Remove();
            Notiflix.Report.Failure('Error', "AJAX error: " + textStatus + " - " + errorThrown, 'Ok');
        }
    });
});

/**
 * Validates the selected file to ensure it is a PDF.
 */
function assignmentfile() {
    if (document.getElementById("file1").files.length !== 0) {
        var mext = $("#file1").val().split('.').pop().toLowerCase();
        if (mext === "pdf") {
            $("#filemessage").text("").removeClass("red");
        } else {
            $("#file1").val("");
            $("#file1").focus();
            Notiflix.Report.Warning('Invalid File Type', 'Assignment should be of PDF only!', 'Ok');
            $("#filemessage").text("Invalid file type. Only PDF allowed.").addClass("red");
            return false;
        }
    }
}

function myFunction(x) {
    x.classList.toggle("change");
}