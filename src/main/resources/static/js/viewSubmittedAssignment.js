$(document).ready(function () {
    $('#submittedassignmenttable').DataTable({
        pageLength: 10,
        lengthMenu: [[5, 10, 20, 50, 99], [5, 10, 20, 50, "99"]]
    });
});

function myFunction(x) {
    x.classList.toggle("change");
}

$("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
});

$(document).ready(function () {
    // Back to top
    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });

    // Sub-menu toggle
    $('.sub-menu ul').hide();
    $('.sub-sub-menu ul').hide();
    $(".sub-menu > a").click(function () { // Target direct anchor children
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });
    $(".sub-sub-menu > a").click(function () {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $("#stuasstform").submit(function (e) {
        e.preventDefault();
        var formData = new FormData($(this)[0]);
        $.ajax({
            type: "POST",
            url: "/nerie/assignments/saveStudentAssignmentMarks",
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                if(data==='-1'){
                    alert("Unable to save. Pls try again");
                } else {
                    alert("Successfully saved");
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert("error:" + textStatus + " - exception:" + errorThrown);
            }
        });
    });

    // $("#stumark").focusout(function () {
    //    alert("Do some savgn");
    // });
});

function savestumark(tess) {
    // alert(tess.id); // Original function was empty
}

function assignmentfile() {
    if (document.getElementById("file1") && document.getElementById("file1").files.length !== 0) { // Check if element exists
        var mext = $("#file1").val().split('.').pop().toUpperCase();
        // var msize = $('#pimgfiles')[0].files[0].size; // Commented out in original reference
        if (((mext === "JPG") || (mext === "JPEG") || (mext === "PDF") || (mext === "TXT"))) {
            var file = $('#file1')[0].files[0];
            if (file) {
                var reader = new FileReader();
                reader.onload = function () {
                    // Assuming #previewfile1 exists if #file1 exists and is used
                    if ($("#previewfile1").length) {
                        $("#previewfile1").attr("src", reader.result);
                    }
                }
                reader.readAsDataURL(file);
            }
        } else {
            $("#file1").val("");
            $("#file1").focus();
            alert("Assignment should be of type jpg, jpeg, pdf, or txt."); // Original message was "jpg" only
            return false;
        }
    }
    return true;
}

$(document).ready(function () {
    var date = new Date();
    var today = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    if ($('#assignmentdate').length) { // Check if element exists
         $('#assignmentdate').datepicker({
            format: 'yyyy-mm-dd', // Note: jQuery UI datepicker uses dateFormat: 'yy-mm-dd'
            orientation: 'bottom' // bootstrap-datepicker option
        });
        $('#assignmentdate').datepicker('setDate', today); // bootstrap-datepicker method
    }

    // The following seems to be jQuery UI datepicker syntax
    if ($("#assignmentdate").length) { // Check if element exists, re-check due to different syntax
         $("#assignmentdate").datepicker({
            dateFormat: "dd-mm-yy" // jQuery UI syntax
        });
    }

    if ($("#lastdate").length) { // Check if element exists
        $("#lastdate").datepicker({
            dateFormat: "dd-mm-yy", // jQuery UI syntax
            minDate: new Date()
        });
    }
});