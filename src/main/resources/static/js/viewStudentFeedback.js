$(document).ready(function () {-
    Notiflix.Notify.Init({
        position: 'right-top',
        timeout: 3000
    });

    let feedTable = $('#feedbacktable').DataTable({
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
            emptyTable: "Select a subject to view student feedback records."
        }
    });

    $('#viewstudentfeedback').on('click', function () {
        const subjectcode = $("#subjectcode").val();

        if (!subjectcode || subjectcode === '-1') {
            Notiflix.Notify.Info('Please select a subject.');
            $("#subjectcode").focus();
            return;
        }

        Notiflix.Loading.Standard('Fetching feedback...');

        feedTable.settings()[0].oLanguage.sEmptyTable = "No feedback found for the selected subject.";

        $.ajax({
            type: "GET",
            url: "/nerie/feedbacks/getFeebackListBasedOnSubjectCode",
            data: { subjectcode: subjectcode },
            dataType: 'json',
            success: function (data) {
                Notiflix.Loading.Remove();
                feedTable.clear();
                if (data && Array.isArray(data) && data.length > 0) {
                    const rows = data.map(item => [
                        item[2], // Student ID
                        item[3], // Student Name
                        item[0], // Feedback
                        item[1]  // Date
                    ]);
                    feedTable.rows.add(rows).draw();
                    Notiflix.Notify.Success(`${data.length} feedback record(s) loaded successfully.`);
                } else {
                    feedTable.draw();
                    Notiflix.Notify.Info("No feedback found for the selected subject.");
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                Notiflix.Loading.Remove();
                Notiflix.Notify.Failure(`An error occurred: ${textStatus}. Please try again.`);
                console.error("AJAX Error:", {
                    status: textStatus,
                    error: errorThrown,
                    response: jqXHR.responseText
                });
                feedTable.clear().draw();
            }
        });
    });

    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    const backToTop = $('#backtotop');
    if (backToTop.length) {
        backToTop.click(function () {
            $("html, body").animate({scrollTop: 0}, 600);
            return false;
        });
    }
    
    const loader = $("#loader");
    if (loader.length) {
         loader.fadeOut("slow");
    }
});