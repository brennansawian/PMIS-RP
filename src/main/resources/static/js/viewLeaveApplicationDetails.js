function myFunction(x) {
    x.classList.toggle("change");
}

$(document).ready(function () {
    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    // Back to top
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
});

// Page-specific scripts
document.addEventListener('DOMContentLoaded', function() {
    const studentLeaveId = document.getElementById('studentLeaveIdHolder') ? document.getElementById('studentLeaveIdHolder').getAttribute('data-studentleaveid') : null;

    const approveButton = document.getElementById('approveBtn');
    if (approveButton && studentLeaveId) {
        approveButton.addEventListener('click', function() {
            populateAcceptModal(studentLeaveId);
        });
    }

    const rejectButton = document.getElementById('rejectBtn');
    if (rejectButton && studentLeaveId) {
        rejectButton.addEventListener('click', function() {
            populateRejectModal(studentLeaveId);
        });
    }

    const acceptSubmitBtn = document.getElementById("accept-submit-btn");
    if (acceptSubmitBtn) {
        acceptSubmitBtn.addEventListener("click", function (e) {
            e.preventDefault();
            acceptleavefunc();
        });
    }

    const rejectSubmitBtn = document.getElementById("reject-submit-btn");
    if (rejectSubmitBtn) {
        rejectSubmitBtn.addEventListener("click", function (e) {
            e.preventDefault();
            rejectleavefunc();
        });
    }
});


// Populate Accept Modal
function populateAcceptModal(studentleaveid) {
    document.getElementById("accept-studentleaveid").value = studentleaveid;
}

// Populate Reject Modal
function populateRejectModal(studentleaveid) {
    document.getElementById("reject-studentleaveid").value = studentleaveid;
}

// Submit Accept Form
function acceptleavefunc() {
    const slid = document.getElementById("accept-studentleaveid").value;
    let rolecodeform = document.getElementById("formrolecode").value;
    const formrolecodeprincideanEl = document.getElementById("formrolecodeprincidean");
    if (formrolecodeprincideanEl && formrolecodeprincideanEl.value === '9') {
        rolecodeform = 9;
    }

    $.ajax({
        type: "POST",
        url: "/nerie/student-leaves/approveLeaveApplication?rolecode=" + rolecodeform, // Relative URL
        data: { studentleaveid: slid },
        success: function (data) {
            if (data === "-1") {
                Notiflix.Report.Failure('Failure', 'Approval Failed!!!', 'Ok');
            } else {
                Notiflix.Report.Success('Success', 'Successfully Forwarded/Approved!!!', 'Ok', function(){
                    window.location.href = "/nerie/student-leaves/approve-student-leave"; // Relative URL
                });
            }
        },
        error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
    });
    return false;
}

// Submit Reject Form
function rejectleavefunc() {
    const slid = document.getElementById("reject-studentleaveid").value;
    const rejectionReason = document.getElementById("RejectReasonDescUpload").value.trim();
    let rolecodeform = document.getElementById("formrolecode").value;

    if (!rejectionReason) {
        Notiflix.Report.Warning('Validation Error', 'Please provide a reason for rejection.', 'Ok');
        return false; // Stop form submission
    }

    const formrolecodeprincideanEl = document.getElementById("formrolecodeprincidean");
     if (formrolecodeprincideanEl && formrolecodeprincideanEl.value === '9') {
        rolecodeform = 9;
    }
    // alert("Effective role code for rejection: " + rolecodeform); // For debugging

    $.ajax({
        type: "POST",
        url: "/nerie/student-leaves/rejectLeaveApplication?rolecode=" + rolecodeform, // Relative URL
        data: {
            studentleaveid: slid,
            rejectionreason: rejectionReason
        },
        // beforeSend function with CSRF token removed
        success: function (data) {
            if (data === "-1") {
                 Notiflix.Report.Failure('Failure', 'Rejection Failed!!!', 'Ok');
            }
            else if (data === "-2") {
                 Notiflix.Report.Warning('Validation Error', 'Rejection Reason Cannot be Empty!!!', 'Ok');
            }
            else {
                Notiflix.Report.Success('Success', 'Application Rejected!!!', 'Ok', function(){
                     window.location.href = "/nerie/student-leaves/approve-student-leave"; // Relative URL
                });
            }
        },
        error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
    });
    return false; // Prevent default form submission
}