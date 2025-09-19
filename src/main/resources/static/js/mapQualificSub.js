// Qualification-subject map save
$(document).ready(function () {
    $(function () {
        $('input.alphabets').keyup(function () {
            if (this.value.match(/[^a-zA-Z. ]/g)) {
                this.value = this.value.replace(/[^a-zA-Z. ]/g, '');
            }
        });
    });

    $("#mmapqsfid").submit(function (e)
    {
        e.preventDefault();
        if ($("#qualificationcode").val() == null) {
            showModalAlert("Please Select Qualification", "Message");
            $("#qualificationcode").focus();
            return false;
        }
        var c = false;
        $('input[type="checkbox"]').each(function () {
            if (this.checked) {
                c = true;
            }
        })
        if (c == false) {
            showModalAlert("Please select subject", "Message");
            return false;
        }

        $.ajax({
            type: "POST",
            url: "/nerie/qualification-subjects/map/save",
            data: $("#mmapqsfid").serialize(),
            success: function (data) {
                if (data == "2") {
                    showModalAlert("Successfully Saved!!!", "Message", function() {
                        window.location.reload();
                    });
                } else {
                    showModalAlert("Save Failed!!!", "Message");
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        })
    })

    $("#qualificationcode").change(function (e) {
        $.ajax({
            type: "GET",
            url: "/nerie/qualification-subjects/get-mp-subjects",
            data: "qualificationcode=" + $('#qualificationcode').val(),
            success: function (data) {
                var c = "";

                for (var i = 0; i < data.length; i++) {
                    if (data[i][2]) {
                        c = c + '<input type="checkbox" value="' + data[i][0] + '" checked="checked" name="subjects"/>' + data[i][1] + '<br />';
                    } else {
                        c = c + '<input type="checkbox" value="' + data[i][0] + '" name="subjects"/>' + data[i][1] + '<br />';
                    }

                    $("#subjectdiv").html(c);
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    });
});

$(document).ready(() => {
    $("#add-new-sub").click(e => {
        e.preventDefault()
        window.location.href = '/nerie/qualification-subjects/manage'
    })

    $('#add-new-qualific').click(e => {
        e.preventDefault()
        window.location.href = '/nerie/qualifications/manage'
    })
})

function showModalAlert(message, title = 'Message', onModalClose = null) {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);

    $('#feedbackModal').off('hidden.bs.modal');

    if (onModalClose && typeof onModalClose === 'function') {
        $('#feedbackModal').on('hidden.bs.modal', function () {
            onModalClose();
        });
    }

    $('#feedbackModal').modal('show');
}


// Modal stuff. Changed to route to it's own page.
// $(document).ready(function () {
//     $("#mqualificationfid").submit(function (e)
//     {
//         e.preventDefault();
//         $.ajax({
//             type: "POST",
//             url: "/nerie/qualification-subjects/init/save",
//             data: $("#mqualificationfid").serialize(),
//             success: function (data) {
//                 if (data == "1") {
//                     alert("Qualification Name Already Exist!!!");
//                     $("#qualificationname").focus();
//                     $("#qualificationname").val("");
//                 } else if (data == "2") {
//                     alert("Successfully Saved!!!");
//                     window.location.href = "/nerie/qualification-subjects/init/map";
//                 } else if (data == "3") {
//                     alert("Qualification Name cannot be Empty!!!");
//                 } else if (data == "4") {
//                     alert("Qualification Name should be 1-100 characters long!!!");
//                 } else {
//                     alert("Save Failed!!!");
//                 }
//             },
//             error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
//         })
//     });
// });

// $(document).ready(function () {
//     $("#mqsubjectfid").submit(function (e)
//     {
//         e.preventDefault();
//         $.ajax({
//             type: "POST",
//             url: "./savequalificationsubjectdetails.htm",
//             data: $("#mqsubjectfid").serialize(),
//             success: function (data) {
//                 if (data == "1") {
//                     alert("Subject Name Already Exist!!!");
//                     $("#qualificationsubjectname").focus();
//                     $("#qualificationsubjectname").val("");
//                 } else if (data == "2") {
//                     alert("Successfully Saved!!!");
//                     window.location.href = "mapqualificationsubject.htm";
//                 } else if (data == "3") {
//                     alert("Suject Name cannot be Empty!!!");
//                 } else if (data == "4") {
//                     alert("Subject Name should be 1-100 characters long!!!");
//                 } else {
//                     alert("Save Failed!!!");
//                 }
//             },
//             error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
//         })
//     })
// });

function customReset() {
    window.location.reload()
}

function myFunction(x) {
    x.classList.toggle("change");
}