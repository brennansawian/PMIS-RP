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
    $('#categorytable').DataTable({
        dom: 'Blfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Program Categories',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('#addft').on('click', function() {
        $('#mcoursecategoriesfid')[0].reset();
        $('#coursecategorycode').val('');
    });

    $("#mcoursecategoriesfid").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/nerie/course-categories/save",
            data: $("#mcoursecategoriesfid").serialize(),
            success: function (data) {
                const response = data.trim();
                switch (response) {
                    case "2":
                        $('#modalid-modal').modal('hide');
                        $('#modalid-modal').one('hidden.bs.modal', function () {
                           showModalAndRedirect('Successfully Saved!', window.location.href);
                        });
                        break;
                    case "1":
                        showModalAlert("Category Name Already Exists!");
                        $("#coursecategoryname").focus().val("");
                        break;
                    case "3":
                        showModalAlert("Category Name cannot be Empty!");
                        break;
                    case "4":
                        showModalAlert("Category Name should be 1-50 characters long!");
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
        if (this.value.match(/[^a-zA-Z.\- ]/g)) {
            this.value = this.value.replace(/[^a-zA-Z.\- ]/g, '');
        }
    });
});

function editfunc(code, name, type) {
    $('#mcoursecategoriesfid')[0].reset();
    $("#coursecategorycode").val(code);
    $("#coursecategoryname").val(name);
    $("#coursetype").val(type);
    $('#modalid-modal').modal('show');
}

function customReset() {
    window.location.reload();
}

document.addEventListener('DOMContentLoaded', function () {
    var editButtons = document.querySelectorAll(".editbtn");

    editButtons.forEach(function (button) {
        button.addEventListener("click", function (event) {
            event.preventDefault();

            var courseCategoryCode = button.getAttribute("data-coursecategorycode");
            var courseCategoryName = button.getAttribute("data-coursecategoryname");
            var courseType = button.getAttribute("data-coursetype");

            editfunc(courseCategoryCode, courseCategoryName, courseType);
        });
    });
});

$("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
});

$(document).ready(function () {
    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });
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