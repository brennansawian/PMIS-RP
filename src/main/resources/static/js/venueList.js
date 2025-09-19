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
    $('#venuetable').DataTable({
        dom: 'Blfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Venues',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('#addft').on('click', function() {
        $('#mvenuefid')[0].reset();
        $('#venuecode').val('');
    });

    $("#mvenuefid").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/nerie/venues/save",
            data: $("#mvenuefid").serialize(),
            success: function (data) {
                const response = data.trim();
                switch (response) {
                    case "2":
                        $('#venue-modal').modal('hide');
                        $('#venue-modal').one('hidden.bs.modal', function () {
                            showModalAndRedirect('Successfully Saved!', window.location.href);
                        });
                        break;
                    case "1":
                        showModalAlert("Venue Already Exists!");
                        $("#venuename").focus().val("");
                        break;
                    case "3":
                        showModalAlert("Venue Name cannot be Empty!");
                        break;
                    case "4":
                        showModalAlert("Venue Name should be 1-100 characters long!");
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
        if (this.value.match(/[^0-9a-zA-Z.\- ]/g)) {
            this.value = this.value.replace(/[^0-9a-zA-Z.\- ]/g, '');
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const editVenueButtons = document.querySelectorAll('.editvenuebtn');

    editVenueButtons.forEach(function (button) {
        button.addEventListener('click', function (event) {
            const venueCode = button.getAttribute('data-param1');
            const venueName = button.getAttribute('data-param2');
            editfunc(venueCode, venueName);
        });
    });
});

function editfunc(code, name) {
    $('#mvenuefid')[0].reset();
    $("#venuecode").val(code);
    $("#venuename").val(name);
}

function customReset() {
    window.location.reload();
}