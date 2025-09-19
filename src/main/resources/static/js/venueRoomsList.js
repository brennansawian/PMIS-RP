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

function resetForm(){
    $('#mvenueroomfid')[0].reset();
    $("#roomcode").val("");
}

function editfunc(roomCode, roomName, capacity, venueCode) {
    resetForm();
    $("#roomcode").val(roomCode);
    $("#roomname").val(roomName);
    $("#capacity").val(capacity);
    $("#venuecode").val(venueCode);
}

$(document).ready(function () {
    $('#roomtable').DataTable({
        dom: 'Blfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Venue Rooms',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('#addft').on('click', function() {
        resetForm();
    });

    $("#mvenueroomfid").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/nerie/venue-rooms/save",
            data: $("#mvenueroomfid").serialize(),
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
                        showModalAlert("Room Name Already Exists!");
                        $("#roomname").focus().val("");
                        break;
                    case "3":
                        showModalAlert("Room Name cannot be Empty!");
                        break;
                    case "4":
                        showModalAlert("Room Name should be 1-50 characters long!");
                        break;
                    default:
                        showModalAlert("Save Failed! An unexpected error occurred.");
                        break;
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                 showModalAlert(`An error occurred: ${textStatus} - ${errorThrown}`);
            }
        });
    });

    $('input.alphabets').keyup(function () {
        if (this.value.match(/[^a-zA-Z. ]/g)) {
            this.value = this.value.replace(/[^a-zA-Z. ]/g, '');
        }
    });

    $(document).on("keypress", ".numbers", function (event) {
        if (event.which < 48 || event.which > 57) {
            event.preventDefault();
        }
    });

    $(document).on("focusout", ".defaultval", function () {
        if ($(this).val().length === 0) {
            $(this).val('0');
        }
    });

    $('#roomtable').on('click', '.editfunc', function() {
        const roomCode = $(this).data("roomcode");
        const roomName = $(this).data("roomname");
        const capacity = $(this).data("capacity");
        const venueCode = $(this).data("venuecode");
        editfunc(roomCode, roomName, capacity, venueCode);
    });

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
});