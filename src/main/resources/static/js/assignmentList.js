document.addEventListener('DOMContentLoaded', function () {
    // Select all buttons with the class 'show-description-btn'
    const showDescriptionButtons = document.querySelectorAll('.show-description-btn');

    // Add event listener to each button
    showDescriptionButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Forward the event to the existing function
            showDescriptionModal(button);
        });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    // Select all buttons with the class 'this_is_upload_button_class'
    const uploadButtons = document.querySelectorAll('.this_is_upload_button_class');

    // Add event listener to each button
    uploadButtons.forEach(button => {
        button.addEventListener('click', function () {
            // Forward the event to the existing showModal function
            showModal(button);
        });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    // Select all links with the 'text-primary' class
    const editLinks = document.querySelectorAll('.text-primary');

    // Add event listener to each link
    editLinks.forEach(link => {
        link.addEventListener('click', function () {
            // Forward the event to the existing editassignment function
            editassignment(link);
        });
    });
});

$(document).ready(() => {
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
    }
    );
})

$(document).ready(function () {
    $('#assignmentlist').DataTable({
        dom: 'lfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]]
    });

    // Feature never implemented in the original code
    // $("#subjectlist").change(function () {
    //     $.ajax({
    //         type: "GET",
    //         url: "./getstudentassinmentlist.htm",
    //         data: "subcode=" + $("#subjectlist").val(),
    //         success: function (data) {
    //             $("#asgnmntid").html("");
    //             var temp = "<select class='form-control'>";
    //             for (i = 0; i < data.length; i++) {
    //                 temp += "<option>" + data[i][1] + "</option>";
    //             }
    //             temp += "</select>"
    //             $("#asgnmntid").append(temp);
    //         },
    //         error: function (jqXHR, textStatus, errorThrown) {
    //             alert("error:" + textStatus + " - exception:" + errorThrown);
    //         }
    //     });
    // });

    $("#afile").change(function () {
        if (document.getElementById("afile").files.length !== 0) {
            var mext = $("#afile").val().split('.').pop();
            mext = mext.toUpperCase();
            //                                        var msize = $('#pimgfiles')[0].files[0].size;
            if (((mext === "PDF"))) {
                var file = $('#afile')[0].files[0];
                if (file) {
                    reader.readAsDataURL(file);
                }
            } else {
                $("#afile").val("");
                $("#afile").focus();
                alert("Assignment should be of: PDF format only.");
                return false;
            }
        }
    });

    // Feature never implemented in the original code
    // $("#uploadformid").submit(function (e) {
    //     e.preventDefault();
    //     if (document.getElementById("afile").files.length === 0) {
    //         alert("Please Upload Assignment File");
    //         return false;
    //     }
    //     var formData = new FormData($(this)[0]);
    //     $.ajax({
    //         type: "POST",
    //         url: "./uploadstudentassignment.htm",
    //         data: formData,
    //         processData: false,
    //         contentType: false,
    //         success: function (data) {
    //             if (data == '-1') {
    //                 Notiflix.Notify.Failure('There was a failure while uploading assignment');
    //             } else {
    //                 $("#exampleModal").modal('hide');
    //                 alert("Successfully Uploaded Assignment");
    //                 window.location.reload();
    //             }
    //         },
    //         error: function (jqXHR, textStatus, errorThrown) {
    //             Notiflix.Notify.Failure('There was a failure while uploading assignment');
    //         }
    //     });
    // });

    $("#editformid").submit(function (e) {
        e.preventDefault();
        
        if (document.getElementById("eafile").files.length === 0) {
            alert("Please Upload Assignment File");
            return false;
        }
        var formData = new FormData($(this)[0]);
        $.ajax({
            type: "POST",
            url: "/nerie/assignments/edit-student-assignment",
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                if (data == '-1') {
                    Notiflix.Notify.Failure('There was a failure while uploading assignment');
                } else {
                    $("#editAssignmentModal").modal('hide');
                    alert("Successfully Edited Assignment");
                    window.location.reload();
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    });
});

function showModal(aid) {
    $("#subname").html($("#subn-" + aid.id).val());
    $("#assignmenttitle").html($("#testn-" + aid.id).val());
    $("#assignmentid").val(aid.id);
    $("#exampleModal").modal('show');
}

function editassignment(aid) {
    $("#esubname").html($("#subn-" + aid.id).val());
    $("#eassignmenttitle").html($("#testn-" + aid.id).val());
    $("#eassignmentid").val(aid.id);
    $("#editAssignmentModal").modal('show');
}

function showDescriptionModal(aid) {
    $("#descriptiontext").html($("#desc-" + aid.id).val());
    $("#descriptionModal").modal('show');
}

var rowclicktddata;
$('#assignmentlist tbody').on('click', 'td button.this_is_upload_button_class', function () {
    rowclicktddata = $(this).closest('td');
});
