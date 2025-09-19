var studyMaterialTable;
var rowclicktddata;

function initializeDataTable() {
    return $('#assignmentlist').DataTable({
        dom: 'Blfrtip',
        pageLength: 10,
        lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Study Materials',
                exportOptions: { columns: ':visible:not(.noExport)' },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });
}

$(document).ready(function () {
    studyMaterialTable = initializeDataTable();

    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });

    $('.sub-menu ul').hide();
    $('.sub-sub-menu ul').hide();

    $(".sub-menu a").click(function () {
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $(".sub-sub-menu a").click(function () {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $("#subcode").change(function () {
        var subcode = $("#subcode").val();
        if (subcode === '-1') {
            alert('Select Subject');
            $("#subcode").focus();
            if (studyMaterialTable) {
                studyMaterialTable.clear().destroy();
            }
            $("#stmattable").html("");
            studyMaterialTable = initializeDataTable(); // Re-init empty table
            return false;
        }

        $.ajax({
            type: "GET",
            url: "/nerie/study-materials/getstudymaterials",
            data: "subjectcode=" + subcode,
            success: function (data) {
                if (studyMaterialTable) {
                    studyMaterialTable.clear().destroy();
                }

                $("#stmattable").html("");

                var temp = "";
                if (data.length === 0 || data === null) {
                    temp += "<tr><td colspan='4'>No Study Material Available</td></tr>";
                } else {
                    for (let i = 0; i < data.length; i++) { // Using 'let' for better scope
                        var s = new Date(data[i].uploaddate).toLocaleDateString("en-US");
                        temp += "<tr><td>" + parseInt(i + 1) + "</td><td>" + data[i].title + "</td><td>" + s + "</td><td><a href='/nerie/study-materials/viewStudyMaterialDocument?sid=" + data[i].studymaterialid + "' target='_blank'>View Study Material</a></td></tr>";
                    }
                }
                $("#stmattable").html(temp);

                studyMaterialTable = initializeDataTable();
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    });

    $("#subjectlist").change(function () {
        $.ajax({
            type: "GET",
            url: "./getstudentassinmentlist.htm",
            data: "subcode=" + $("#subjectlist").val(),
            success: function (data) {
                $("#asgnmntid").html("");
                var temp = "<select class='form-control'>";
                for (let i = 0; i < data.length; i++) {
                    temp += "<option>" + data[i][1] + "</option>";
                }
                temp += "</select>"
                $("#asgnmntid").append(temp);
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    });

    $("#afile").change(function () {
        if (document.getElementById("afile").files.length !== 0) {
            var mext = $("#afile").val().split('.').pop().toUpperCase();
            if (!((mext === "JPG") || (mext === "JPEG") || (mext === "PDF") || (mext === "TXT"))) {
                $("#afile").val("").focus();
                alert("Assignment should be of: .jpg, .pdf, .txt formats only.");
                return false;
            }
        }
    });

    $("#uploadformid").submit(function (e) {
        e.preventDefault();
        if (document.getElementById("afile").files.length === 0) {
            alert("Please Upload Assignment File");
            return false;
        }
        var formData = new FormData($(this)[0]);
        $.ajax({
            type: "POST",
            url: "./uploadstudentassignment.htm",
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                var anchorstring = '<a class="btn btn-success" href="viewassignmentsubmission.htm?fid=' + data.toString() + '" target="_blank">View My Assignment</a>';
                rowclicktddata.html(anchorstring);
                $("#exampleModal").modal('hide');
                Notiflix.Notify.Success('Assignment Successfully Submitted');
            },
            error: function (jqXHR, textStatus, errorThrown) {
                Notiflix.Notify.Failure('There was a failure while uploading assignment');
            }
        });
    });

    var date = new Date();
    var today = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    $('#assignmentdate').datepicker({
        format: 'yyyy-mm-dd',
        orientation: 'bottom'
    });
    $('#assignmentdate').datepicker('setDate', today);

    $("#lastdate").datepicker({
        dateFormat: "dd-mm-yy",
        minDate: new Date()
    });

    $('#assignmentlist tbody').on('click', 'td button.this_is_upload_button_class', function () {
        rowclicktddata = $(this).closest('td');
    });
});

function showModal(aid) {
    $("#subname").html($("#subn").val());
    $("#assignmenttitle").html($("#testn").val());
    $("#assignmentid").val(aid.id);
    $("#exampleModal").modal('show');
}