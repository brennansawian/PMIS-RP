function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

function showModalAndRedirect(message, url) {
    showModalAlert(message);
    $('#feedbackModal').one('hidden.bs.modal', function () {
        window.location.href = url;
    });
}

$(document).ready(function () {
    $('#qsubjecttable').DataTable({
        dom: 'Blfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Qualifications Subjects',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('#addft').on('click', function() {
        $('#mqsubjectfid')[0].reset();
        $('#qualificationsubjectcode').val('');
    });

    $("#mqsubjectfid").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/nerie/qualification-subjects/save",
            data: $("#mqsubjectfid").serialize(),
            success: function (data) {
                const response = data.trim();
                switch (response) {
                    case "2":
                        $('#modalid-modal').modal('hide');
                        $('#modalid-modal').one('hidden.bs.modal', function() {
                            showModalAndRedirect("Successfully Saved!", window.location.href);
                        });
                        break;
                    case "1":
                        showModalAlert("Subject Name Already Exists!");
                        $("#qualificationsubjectname").focus().val("");
                        break;
                    case "3":
                        showModalAlert("Subject Name cannot be Empty!");
                        break;
                    case "4":
                        showModalAlert("Subject Name should be 1-100 characters long!");
                        break;
                    default:
                        showModalAlert("Save Failed! An unexpected error occurred.");
                        break;
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status === 401) {
                    showModalAndRedirect("Authentication error. Please log in again.", '/nerie/login');
                } else {
                    showModalAlert(`An error occurred: ${textStatus} - ${errorThrown}`);
                }
            }
        });
    });

    $('input.alphabets').keyup(function () {
        if (this.value.match(/[^a-zA-Z.\- ]/g)) {
            this.value = this.value.replace(/[^a-zA-Z.\- ]/g, '');
        }
    });

    $('#backtotop').click(function () {
        $("html, body").animate({ scrollTop: 0 }, 600);
        return false;
    });

    $(".sub-menu ul").hide();
    $(".sub-sub-menu ul").hide();
    $(".sub-menu a").click(function () {
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });
    $(".sub-sub-menu a").click(function () {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });
});

function customReset() {
    window.location.reload();
}

function editfunc(code, name) {
    $('#mqsubjectfid')[0].reset();
    $("#qualificationsubjectcode").val(code);
    $("#qualificationsubjectname").val(name);
}

document.addEventListener('DOMContentLoaded', function () {
    const editButtons = document.querySelectorAll(".edit-subject-btn");

    editButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            const subjectCode = button.getAttribute("data-subjectcode");
            const subjectName = button.getAttribute("data-subjectname");
            editfunc(subjectCode, subjectName);
        });
    });
});