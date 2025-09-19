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
    $('#qualificationtable').DataTable({
        dom: 'Blfrtip',
        pageLength: 10,
        lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Qualifications',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('#addft').on('click', function() {
        $('#mqualificationfid')[0].reset();
        $('#qualificationcode').val('');
    });

    $("#mqualificationfid").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/nerie/qualifications/save",
            data: $("#mqualificationfid").serialize(),
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
                        showModalAlert("Qualification Name Already Exists!");
                        $("#qualificationname").focus().val("");
                        break;
                    case "3":
                        showModalAlert("Qualification Name cannot be Empty!");
                        break;
                    case "4":
                        showModalAlert("Qualification Name should be 1-100 characters long!");
                        break;
                    default:
                        showModalAlert("Save Failed! An unexpected error occurred.");
                        break;
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                 showModalAlert(`An error occurred while saving. Please try again.<br><b>Error:</b> ${errorThrown}`);
            }
        });
    });

    $('input.alphabets').keyup(function () {
        if (this.value.match(/[^a-zA-Z. ]/g)) {
            this.value = this.value.replace(/[^a-zA-Z. ]/g, '');
        }
    });

    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const editButtons = document.querySelectorAll(".qualification-edit-btn");

    editButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            const qcode = button.getAttribute("data-qcode");
            const qname = button.getAttribute("data-qname");
            const qcategory = button.getAttribute("data-qcategory");
            editfunc(qcode, qname, qcategory);
        });
    });
});

$("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
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

function editfunc(code, name, category) {
    $('#mqualificationfid')[0].reset();
    $("#qualificationcode").val(code);
    $("#qualificationname").val(name);
    $("#qualificationcategorycode").val(category);
}

function customReset() {
    window.location.reload();
}