let feedbackTable;

$(document).ready(function () {
    $("#statsrow").hide();

    feedbackTable = $('#approveprogram').DataTable({
        dom: 'Blfrtip',
        pageLength: 5,
        destroy: true,
        columns: [
            { title: "Sl No." },
            { title: "Feedback" },
            { title: "User" }
        ],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Daily Feedback List',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('.getlist').on('click', function () {
        getdayfeedbacks();
    });
});

function getdayfeedbacks() {
    const programCode = $("#programtimetablecode").val();

    if (programCode === "") {
        alert("Please Select Subject");
        return;
    }

    $.ajax({
        type: "GET",
        url: "/nerie/participant/feedback/daily-feedback/get",
        data: {
            programtimetablecode: programCode
        },
        success: function (data) {
            feedbackTable.clear(); // clear old data

            if (data.length > 0) {
                const newRows = data.map((item, index) => {
                    return [
                        index + 1,
                        item.feedback || '',
                        item.usercode?.username || ''
                    ];
                });

                feedbackTable.rows.add(newRows).draw(); // add and draw
                $("#statsrow").show();
            } else {
                alert("No Feedbacks Yet");
                $("#statsrow").hide();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert("Error: " + textStatus + " - " + errorThrown);
        }
    });
}
