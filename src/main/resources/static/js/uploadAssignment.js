$(document).ready(function () {

    $('#assignmentslist').DataTable({
        dom: 'Blfrtip',
        pageLength: 10,
        lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Assignments',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('#assignmentslist tbody').on('click', '.edit-button', function () {
        showEditModal(this);
    });

    $("#assignmentid").submit(function (e) {
            e.preventDefault();

            var passMarkValue = parseInt($("#passmarkCreate").val(), 10);
            var fullMarkValue = parseInt($("#fullmarkCreate").val(), 10);

            if (passMarkValue >= fullMarkValue) {
                showModalMessage("Pass Mark cannot be greater than or equal to Full Mark.");
                return false;
            }

            var formData = new FormData(this);
            $.ajax({
                type: "POST",
                url: $(this).attr('action'),
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data === '-1') {
                        showModalMessage("Error. Something happened.");
                    } else {
                        showModalMessage("Assignment Successfully Uploaded.");

                        $('#feedbackModal').one('hidden.bs.modal', function () {
                            window.location.reload();
                        });
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    showModalMessage("Error: " + textStatus + " - Exception: " + errorThrown);
                }
            });
        });

    $("#editassignmentForm").submit(function (e) {
        e.preventDefault();

        var passMarkValue = parseInt($("#edpassmarkEdit").val(), 10);
        var fullMarkValue = parseInt($("#edfullmarkEdit").val(), 10);

        if (passMarkValue >= fullMarkValue) {
            $("#feedbackModalBody").html("Pass Mark cannot be greater than or equal to Full Mark.");
            $("#feedbackModal").modal("show");
            return false;
        }

        var formDataed = new FormData(this);
        $.ajax({
            type: "POST",
            url: $(this).attr('action'),
            data: formDataed,
            processData: false,
            contentType: false,
            success: function (data) {
                if (data === '-1') {
                    showModalMessage("Error. Something happened.");
                } else {
                    $('#editModal').one('hidden.bs.modal', function () {
                        showModalMessage("Assignment Successfully Updated.");

                        $('#feedbackModal').one('hidden.bs.modal', function () {
                            window.location.reload();
                        });
                    });

                    $('#editModal').modal('hide');
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showModalMessage("Edit Error:" + textStatus + " - exception:" + errorThrown);
            }
        });
    });


    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });

    $('.sub-menu ul, .sub-sub-menu ul').hide();

    $(".sub-menu > a").click(function () {
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });
    $(".sub-sub-menu > a").click(function () {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });


    $('.datepicker').datepicker({
        dateFormat: "dd-mm-yy",
        orientation: 'bottom',
        autoclose: true
    });

    var today = new Date();
    $("#lastdateCreate, #edlastdateEdit").datepicker('option', 'minDate', today);
    $("#assignmentdateCreate, #edassignmentdateEdit").datepicker('option', 'minDate', today);

    $("#assignmentdateCreate").on("change", function() {
        var selectedDate = $(this).datepicker("getDate");
        $("#lastdateCreate").datepicker("option", "minDate", selectedDate);
        var currentLastDate = $("#lastdateCreate").datepicker("getDate");
        if (currentLastDate && currentLastDate < selectedDate) {
            $("#lastdateCreate").datepicker("setDate", selectedDate);
        }
    });

    $("#AssignmentDescUploadCreate").keyup(function () {
        $("#charactercountdescCreate").html("Characters left: " + (500 - $(this).val().length));
    });
    $("#edAssignmentDescUploadEdit").keyup(function () {
        $("#edcharactercountdescEdit").html("Characters left: " + (500 - $(this).val().length));
    });


    // CREATE form
    $("#fileRadioCreate, #linkRadioCreate").on("change", function() {
        if ($("#fileRadioCreate").is(":checked")) {
            $("#fileUploadDivCreate").show();
            $("#linkInputDivCreate").hide();
            $("#file1Create").prop("required", true);
            $("#linkInputCreate").prop("required", false).val('');
        } else if ($("#linkRadioCreate").is(":checked")) {
            $("#fileUploadDivCreate").hide();
            $("#linkInputDivCreate").show();
            $("#file1Create").prop("required", false).val('');
            $("#linkInputCreate").prop("required", true);
        }
    });

    if ($("#fileRadioCreate").is(":checked")) {
        $("#fileRadioCreate").trigger('change');
    } else if ($("#linkRadioCreate").is(":checked")) {
        $("#linkRadioCreate").trigger('change');
    } else {
        $("#fileUploadDivCreate").hide();
        $("#linkInputDivCreate").hide();
    }

    // EDIT form
    $("#edfileRadioEdit, #edlinkRadioEdit").on("change", function() {
        if ($("#edfileRadioEdit").is(":checked")) {
            $("#edfileInputDivEdit").show();
            $("#edlinkInputDivEdit").hide();
            $("#edfile1Edit").prop("required", true);
            $("#edlinkInputEdit").prop("required", false).val('');
        } else if ($("#edlinkRadioEdit").is(":checked")) {
            $("#edfileInputDivEdit").hide();
            $("#edlinkInputDivEdit").show();
            $("#edfile1Edit").prop("required", false).val('');
            $("#edlinkInputEdit").prop("required", true);
        }
    });
});


function myFunction(x) {
    x.classList.toggle("change");
}

function showEditModal(button) {
    const assignmentId = $(button).data("id");
    const title = $(button).data("title");
    const description = $(button).data("description");
    const subject = $(button).data("subject");
    const uploadDate = $(button).data("uploaddate");
    const submissionDate = $(button).data("submissiondate");
    const fullMark = $(button).data("fullmark");
    const passMark = $(button).data("passmark");
    const submissionType = $(button).data("submissiontype");
    const linkUrl = $(button).data("linkurl");

    // Populate modal fields
    const $modal = $("#editModal");
    $modal.find("#assignmentIdEdit").val(assignmentId);
    $modal.find("#edassignmentNameEdit").val(title);
    $modal.find("#edAssignmentDescUploadEdit").val(description);
    $modal.find("#subjectCodeEdit").val(subject);
    $modal.find("#edassignmentdateEdit").val(uploadDate);
    $modal.find("#edlastdateEdit").val(submissionDate);
    $modal.find("#edfullmarkEdit").val(fullMark);
    $modal.find("#edpassmarkEdit").val(passMark);

    // Handle radio buttons and conditional inputs
    if (submissionType === 'LINK') {
        $modal.find("#edlinkRadioEdit").prop("checked", true);
        $modal.find("#edlinkInputEdit").val(linkUrl);
    } else if (submissionType === 'FILE') {
        $modal.find("#edfileRadioEdit").prop("checked", true);
    }

    $modal.find('input[name="submissiontype"]:checked').trigger('change');

    $modal.find("#edAssignmentDescUploadEdit").trigger('keyup');

    $modal.modal('show');
}

function assignmentfile(context) {
    var fileInputId = (context === 'Edit') ? "edfile1Edit" : "file1Create";
    var fileInput = $("#" + fileInputId);
    if (fileInput.get(0).files.length > 0) {
        var mext = fileInput.val().split('.').pop().toUpperCase();
        if (mext !== "PDF") {
            fileInput.val("");
            showModalMessage("Only PDF files are allowed.");
            return false;
        }
    }
    return true;
}

function showModalMessage(message) {
    $("#feedbackModalBody").html(message);
    $("#feedbackModal").modal("show");
}