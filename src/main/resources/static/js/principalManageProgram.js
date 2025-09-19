document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".acceptbtn").forEach((button) => {
        button.addEventListener("click", function (event) {
            event.preventDefault(); // Prevent default behavior if needed

            let programId = this.getAttribute("data-program-id");
            let someValue = this.getAttribute("data-some-value");

            // Call the function with extracted values
            acceptprogramfunc(programId, someValue);
        });
    });
});

document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".rejectbtn").forEach((button) => {
        button.addEventListener("click", function (event) {
            event.preventDefault(); // Prevent default behavior if needed

            let programId = this.getAttribute("data-program-id");
            let someValue = this.getAttribute("data-some-value");

            // Call the reject function with extracted values
            rejectprogramfunc(programId, someValue);
        });
    });
});

document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".detetebtn").forEach((button) => {
        button.addEventListener("click", function (event) {
            event.preventDefault(); // Prevent default behavior if needed

            let programId = this.getAttribute("data-program-id");
            let someValue = this.getAttribute("data-some-value");

            // Call the delete function with extracted values
            deleteprogramfunc(programId, someValue);
        });
    });
});

$(document).ready(function () {
    $("#backtotop").click(function () {
        $("html, body").animate({ scrollTop: 0 }, 600);
        return false;
    });
});

$(document).ready(function () {
    $("#approveprogram").DataTable({
        dom: "Blfrtip",
        pageLength: 10,
        lengthMenu: [
            [5, 10, 20, 50, -1],
            [5, 10, 20, 50, "All"],
        ],
        buttons: [
            {
                extend: "excel",
                text: '<em><i class="fa fa-file-excel-o">Excel</i></em>',
                title: function () {
                    return "Programs";
                },
                exportOptions: {
                    columns: "thead th:not(.noExport)",
                },
            },
        ],
    });
});

$(document).ready(function () {
    $("#acceptprogramfid").submit(function (e) {
        e.preventDefault();
        checkapprovedoctype();
        var formData = new FormData($(this)[0]);
        $.ajax({
            type: "POST",
            url: "/nerie/program-details/principal-director/accept",
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                if (data === "2") {
                    alert("Successfully Saved!!!");
                    window.location.reload()
                } else if (data === "1") {
                    alert("Please Upload Approval Letter");
                } else if (data === "4") {
                    alert(
                        "Uploaded File is not allowed. Kindly check filetype or filename"
                    );
                    $('input[type="file"]').val(null);
                } else {
                    alert("Save Failed!!!");
                    $('input[type="file"]').val(null);
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    });

    $("#rejectprogramfid").submit(function (e) {
        e.preventDefault();
        checkrejectdoctype();
        var formData = new FormData($(this)[0]);
        $.ajax({
            type: "POST",
            url: "/nerie/program-details/principal-director/reject",
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                if (data == "2") {
                    alert("Successfully Saved!!!");
                    window.location.reload()
                } else if (data == "1") {
                    alert("Please Upload Rejection Letter");
                } else if (data == "4") {
                    alert(
                        "Uploaded File is not allowed. Kindly check filetype or filename"
                    );
                    $('input[type="file"]').val(null);
                } else {
                    alert("Save Failed!!!");
                    $('input[type="file"]').val(null);
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown) 
        });
    });
});

function acceptprogramfunc(pdid, cname) {
    $("#acceptprogramname").html(cname);
    $("#aprogramdetailid").val(pdid);
}

function rejectprogramfunc(pdid, cname) {
    $("#rejectprogramname").html(cname);
    $("#rprogramdetailid").val(pdid);
}

function deleteprogramfunc(programcode, programname) {
    $.confirm({
        title:
            "Are you sure you want to delete the Program " + programname + " ?",
        content: "",
        buttons: {
            cancel: function () {
                $.alert("Cancelled!");
            },
            delete: {
                text: "Delete",
                btnClass: "btn-red",
                action: function () {
                    $.confirm({
                        content: function () {
                            var self = this;
                            return $.ajax({
                                method: "GET",
                                url: "/nerie/program-details/principal-director/delete",
                                data: "programcode=" + programcode,
                            })
                                .done(function (response) {
                                    if (response === "1") {
                                        self.setTitle("Successful!");
                                        self.setContent(
                                            "Program was Deleted succesfully"
                                        );
                                    } else {
                                        self.setTitle("Error Occurred!");
                                        self.setContent(
                                            "Unable to Delete user. Please try again!"
                                        );
                                    }
                                })
                                .fail(function (x) {
                                    self.setTitle("");
                                    self.setContent("Something went wrong.");
                                });
                        },
                        onDestroy: function () {
                            window.location.reload();
                        },
                    });
                },
            },
        },
    });
}

function checkapprovedoctype() {
    if (document.getElementById("file1").files.length !== 0) {
        var mext = $("#file1").val().split(".").pop();
        if (
            mext !== "jpg" &&
            mext !== "JPG" &&
            mext !== "jpeg" &&
            mext !== "JPEG" &&
            mext !== "pdf" &&
            mext !== "PDF"
        ) {
            alert("Approval Letter should be of type jpg/jpeg/pdf only ");
            $("#file1").val("");
            $("#file1").focus();
            return false;
        }
    }
}
function checkrejectdoctype() {
    if (document.getElementById("file2").files.length !== 0) {
        var mext = $("#file2").val().split(".").pop();
        if (
            mext !== "jpg" &&
            mext !== "JPG" &&
            mext !== "jpeg" &&
            mext !== "JPEG" &&
            mext !== "pdf" &&
            mext !== "PDF"
        ) {
            alert("Rejection Letter should be of type jpg/jpeg/pdf only ");
            $("#file2").val("");
            $("#file2").focus();
            return false;
        }
    }
}
