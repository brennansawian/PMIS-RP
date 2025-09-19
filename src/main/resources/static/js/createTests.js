$(document).ready(function () {
    $('#usertable').DataTable({
        dom: 'Blfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Tests',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    // Form Submission
    $("#testsdetailsform").submit(function (e) {
        e.preventDefault();
        var form = $(this);

        $.ajax({
            type: "POST",
            url: form.attr("action"),
            data: form.serialize(),
            success: function (data) {
                var responseData = String(data).trim();
                if (responseData === '-1') {
                    showFeedbackModal("There was an error saving the Test.");
                } else if (responseData === '1') {
                    showFeedbackModal("Successfully Saved the Test.", true);
                } else {
                    showFeedbackModal("Test submission status: " + responseData, true);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                var errorMessage = "An error occurred during submission.";
                if (jqXHR.status === 403) {
                    errorMessage = "Access Denied (403 Forbidden). You may not have the necessary permissions for this action.";
                } else if (jqXHR.responseText) {
                    errorMessage = "Error: " + textStatus + " - " + errorThrown + "\nServer response: " + jqXHR.responseText;
                } else {
                    errorMessage = "Error: " + textStatus + " - " + errorThrown;
                }
                showFeedbackModal(errorMessage);
            }
        });
    });

    // Datepicker Initialization
    var date = new Date();
    var today = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    $('.datepicker').datepicker({
        dateFormat: 'dd-mm-yy',
        orientation: 'bottom'
    });

    if ($('#testdate').val() === '') {
         $('#testdate').datepicker('setDate', today);
    }

    $('#customresetbutton').on('click', function() {
        customReset();
    });

    $('#usertable tbody').on('click', '.editbtn', function(event) {
        event.preventDefault();
        var button = $(this);
        var testid = button.data('param1');
        var testname = button.data('param2');
        var testno = button.data('param3');
        var subjectcode = button.data('param4');
        var testdateStr = button.data('param5');
        var passmark = button.data('param6');
        var fullmark = button.data('param7');
        edittest(testid, testname, testno, subjectcode, testdateStr, passmark, fullmark);
    });

    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });

    // Sidebar menu functionality
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

    if ($('#loader').length) {
         $('#loader').fadeOut('slow');
    }
});

function customReset() {
    $('#testsdetailsform')[0].reset();
    $("#testid").val('');
    $("#subjectcode").prop('selectedIndex', 0);
    var today = new Date();
    $('#testdate').datepicker('setDate', today);
    $("#subjectcode").focus();
}

function edittest(testid, testname, testno, subjectcode, testdateStr, passmark, fullmark) {
    // Populate form fields
    $("#testid").val(testid);
    $("#testname").val(testname);
    $("#testno").val(testno);
    $("#subjectcode").val(String(subjectcode));
    $("#passmark").val(passmark);
    $("#fullmark").val(fullmark);

    if (testdateStr && testdateStr.trim() !== '') {
        $("#testdate").datepicker('setDate', testdateStr);
    } else {
        $("#testdate").val('');
        $("#testdate").datepicker('setDate', null);
    }

    $('html, body').animate({ scrollTop: 0 }, 'fast');

    $("#subjectcode").focus();
}

function showFeedbackModal(message, reloadOnClose = false) {
    $("#feedbackModalBody").text(message);

    $('#feedbackModal').off('hidden.bs.modal');

    if (reloadOnClose) {
        $('#feedbackModal').on('hidden.bs.modal', function () {
            window.location.reload();
        });
    }

    $("#feedbackModal").modal("show");
}

if ($("#menu-toggle").length) {
    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });
}