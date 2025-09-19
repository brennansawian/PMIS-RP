$(document).ready(function () {
    $("#studentleaveid").submit(function (ed) {
        ed.preventDefault()

        const phno = $("#guardian-phno").val();
        if (phno.length !== 10) {
            alert("Please enter a valid 10-digit phone number.");
            return false;
        }

        // Get the Leave From and Leave To dates
        const leaveFromDateStr = $("#requestedfromdate").val(); // Example: "DD-MM-YYYY"
        const leaveToDateStr = $("#requestedtodate").val();

        // Parse dates assuming the format is DD-MM-YYYY
        function parseDate(dateStr) {
            const [day, month, year] = dateStr.split("-").map(Number);
            return new Date(year, month - 1, day); // Month is zero-based
        }

        const leaveFromDate = parseDate(leaveFromDateStr);
        const leaveToDate = parseDate(leaveToDateStr);

        //                    alert(leaveFromDate);
        //                    alert(leaveToDate);

        // Check if Leave To date is earlier than Leave From date
        if (leaveToDate < leaveFromDate) {
            alert("Starting date of leave cannot be earlier than Ending Date.");
            return false;
        }

        const isDayScholar = $("#isDayScholar").is(":checked");
        ed.preventDefault();
        var formDataed = new FormData($(this)[0]);
        // formDataed.forEach((value, key) => {
        //     console.log(key, value);
        //     //alert(value);
        // });
        $.ajax({
            type: "POST",
            url: "/nerie/student-leaves/submit-application?ds=" + isDayScholar,
            data: formDataed,
            processData: false,
            contentType: false,
            success: function (data) {
                if (data === "-1") {
                    alert("Error. Something happened.");
                } else {
                    alert("Application Successfully Uploaded.");
                    window.location.reload();
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown),
        });
    });
});

$(document).ready(() => {
    $("#requestedtodate").datepicker({
        dateFormat: "dd-mm-yy",
        minDate: new Date(),
    });

    $("#requestedfromdate").datepicker({
        dateFormat: "dd-mm-yy",
        minDate: new Date(),
    });

    $("#AssignmentDescUpload").keyup(function () {
        $("#charactercountdesc").html(
            "Characters left: " + (250 - $(this).val().length)
        );
    });

    $("#declarationCheckbox").change(function () {
        $("#subbtn").prop('disabled', !$(this).is(":checked"));
    });
});

function assignmentfile1() {
    if (document.getElementById("file1").files.length !== 0) {
        var mext = $("#file1").val().split(".").pop();
        mext = mext.toUpperCase();
        //                                        var msize = $('#pimgfiles')[0].files[0].size;
        if (mext === "PDF") {
            var file = $("#file1")[0].files[0];
            if (file) {
                var reader = new FileReader();
                reader.onload = function () {
                    $("#previewfile1").attr("src", reader.result);
                };
                reader.readAsDataURL(file);
            }
        } else {
            $("#file1").val("");
            $("#file1").focus();
            alert("Assignment should be of type PDF only ");
            return false;
        }
    }
}

function assignmentfile2() {
    if (document.getElementById("file2").files.length !== 0) {
        var mext = $("#file1").val().split(".").pop();
        mext = mext.toUpperCase();
        //                                        var msize = $('#pimgfiles')[0].files[0].size;
        if (mext === "PDF") {
            var file = $("#file2")[0].files[0];
            if (file) {
                var reader = new FileReader();
                reader.onload = function () {
                    $("#previewfile1").attr("src", reader.result);
                };
                reader.readAsDataURL(file);
            }
        } else {
            $("#file2").val("");
            $("#file2").focus();
            alert("Assignment should be of type PDF only ");
            return false;
        }
    }
}
