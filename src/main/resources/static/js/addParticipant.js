var ptable;
var programcode;
var ifemailexist = "N";

document.addEventListener("DOMContentLoaded", () => {
    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $("#backtotop").click(function () {
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

document.addEventListener("DOMContentLoaded", function () {
    // Get the button element by its ID
    var addButton = document.getElementById("addft");
    // Check if the button exists
    if (addButton) {
        // Add the event listener for the 'click' event
        addButton.addEventListener("click", function () {
            // Call the function when the button is clicked
            addparticipantfunc();
        });
    }

    // Select all buttons with the class 'resetformbtn'
    var resetButtons = document.querySelectorAll(".resetformbtn");

    // Loop through each button and add the event listener
    resetButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            // Call the resetform function when the button is clicked
            resetform();
        });
    });
});


document.addEventListener("DOMContentLoaded", () => {
    // console.log("DEOS");
    ptable = initdatatable("addparticipanttable", "Participant List");
    
    $("#uploadbtn").hide();
    
    $("#financialyear").change(function () {
        //get list of programs in this financial year
        var fy = $("#financialyear").val();
        
        $("#programs,#phaseno").empty();
        $("#programs,#phaseno").append(
            $("<option></option>").attr("value", "").text("Select")
        );

        if (fy) {
            var fystart = fy.split("##")[0];
            var fyend = fy.split("##")[1];

            callCustomAjax(
                "/nerie/program/financial-year/list",
                "fystart=" + fystart + "&fyend=" + fyend,
                function (data) {
                    if (data) {
                        data.forEach(function (x) {
                            $("#programs").append(
                                $("<option></option>")
                                    .attr("value", x[0])
                                    .text(x[1])
                                    .attr("title", x[1])
                            );
                        });
                        $("#programs > option").text(function (i, text) {
                            if (text.length > 100) {
                                return text.substr(0, 100) + "...";
                            }
                        });
                    }
                }
            );
        }
    });

    $("#programs").change(function () {
        $("#phaseno").empty();
        $("#phaseno").append(
            $("<option></option>").attr("value", "").text("Select")
        );
        if ($("#programs").val()) {
            callCustomAjax(
                "/nerie/phases/list",
                "programcode=" + $("#programs").val(),
                function (data) {
                    if (data) {
                        data.forEach(function (x) {
                            $("#phaseno").append(
                                $("<option></option>").attr("value", x[0]).text(x[1])
                            );
                        });
                    }
                }
            );
        }
    });

    $("#phaseno").change(function () {
        if ($("#phaseno").val()) {
            $("#phaseid").val($("#phaseno").val());
            getparticipantsfunc();
        }
    });
});

$(document).ready(function () {
    $("#uploadbtn").hide();
    $(document).on("keypress", ".numbers", function (event) {
        if (event.which < 48 || event.which > 57) {
            event.preventDefault();
        }
    });
    $(function () {
        $("input.alphabets").keyup(function () {
            if (this.value.match(/[^a-zA-Z. ]/g)) {
                this.value = this.value.replace(/[^a-zA-Z. ]/g, "");
            }
        });
    });
    $("#usermobile").focusout(function () {
        var m = $("#usermobile").val();
        if (m.length > 0 && m.length < 10) {
            $("#msg1").html("Mobile no.should be 10 digit");
            $("#usermobile").focus();
            return false;
        } else {
            $("#msg1").html("");
        }
    });
    $("#usermobile").keypress(function () {
        var m = $("#usermobile").val();
        if (m.length == 9) {
            $("#msg1").html("");
        }
    });
    $("#emailid").focusout(function (e) {
        var re =
            /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
        if ($("#emailid").val().replace(/\s/g, "").length != 0) {
            if (re.test($("#emailid").val()) == false) {
                alert("Please enter valid Email ID");
                //                            e.preventDefault();
                $("#emailid").val("");
                $("#emailid").focus();
                //                            return false;
            } else {
                $.ajax({
                    type: "POST",
                    url: "/nerie/participant/get-participant",
                    data: "userid=" + $("#emailid").val(),
                    success: function (data) {
                        if (data.length > 0) {
                            alert(
                                "Email ID already registered kindly add participant to the Program"
                            );
                            $("#username").prop("readonly", true);
                            $("#usermobile").prop("readonly", true);
                            //                                        $("#statecode").prop("disabled", true);
                            $("#usercode").val(data[0][0]);
                            $("#username").val(data[0][1]);
                            $("#usermobile").val(data[0][3]);
                            $("#statecode").val(data[0][4]);
                            ifemailexist = "Y";
                        } else {
                            $("#username").prop("readonly", false);
                            $("#usermobile").prop("readonly", false);
                            //                                        $("#statecode").prop("disabled", false);
                            $("#usercode").val("");
                            $("#username").val("");
                            $("#usermobile").val("");
                            $("#statecode").val("");
                        }
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        alert("error:" + textStatus + " - exception:" + errorThrown);
                    },
                });
            }
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    $("#mtuserloginfid").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/nerie/participant/create",
            data: $("#mtuserloginfid").serialize(),
            success: function (data) {
                if (data[0] === "2") {
                    alert("Successfully Added!!!");
                    if (data[1]) {
                        alert(data[1]);
                        $("#modalsubj-modal").modal("hide");
                    }
                    getparticipantsfunc();
                    resetform();
                    window.location.reload()
                } else if (data[0] == "1") {
                    alert("Successfully Added");
                    getparticipantsfunc();
                    resetform();
                    window.location.reload()
                } else
                    alert("error:" + textStatus + " - exception:" + errorThrown);
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    });
});

function myFunction(x) {
    x.classList.toggle("change");
}

function getparticipantsfunc() {
    callCustomAjax(
        "/nerie/participant/list",
        "phaseid=" + $("#phaseno").val(),
        function (data) {
            ptable.clear();
            ptable.destroy();
            if (data) {
                count = 1;
                data.forEach(function (x) {
                    var rd =
                        "<tr>" +
                        "<td>" +
                        count++ +
                        "</td>" +
                        "<td>" +
                        x[4] +
                        "</td>" +
                        "<td>" +
                        x[2] +
                        "</td>" +
                        "<td>" +
                        x[1] +
                        "</td>" +
                        "<td>" +
                        x[3] +
                        "</td>" +
                        "<td>" +
                        x[7] +
                        "</td>" +
                        "<td>";

                    if (x[5] === "P") {
                        rd += "Pending";
                    } else if (x[5] === "A") {
                        rd += "Accepted";
                    } else if (x[5] === "R") {
                        rd += "Rejected";
                    }
                    rd += "</td>" + " <td>";
                    if (x[5] === "P") {
                        rd +=
                            '<a class="clickme danger11 editbtn" data-toggle="modal" data-target="#modalsubj-modal" data-dismiss="modal" ' +
                            'data-x0="' +
                            x[0] +
                            '" data-x1="' +
                            x[1] +
                            '" data-x2="' +
                            x[2] +
                            '" data-x3="' +
                            x[3] +
                            '" ' +
                            'data-x6="' +
                            x[6] +
                            '" data-x8="' +
                            x[8] +
                            '"><i class="fa fa-edit"> &nbsp;Edit</i></a>' +
                            '<br><br><a class="clickme enablebtn deletebtn" data-x4="' +
                            x[4] +
                            '" data-x1="' +
                            x[1] +
                            '">' +
                            '<span style="color: #fff"><i class="fa fa-trash"> &nbsp;Delete</span></a>';
                    }

                    rd += "</td>" + "</tr>";
                    $("#addparticipanttable tbody").append(rd);
                });
                $("#tablediv").show();
                $("#uploadbtn").show();
            } else {
                $("#tablediv").hide();
                alert("No data found");
            }
            ptable = initdatatable("addparticipanttable", "Participant List");

            // Select all 'Edit' buttons and 'Delete' buttons
            var editButtons = document.querySelectorAll(".editbtn");
            var deleteButtons = document.querySelectorAll(".deletebtn");

            // Add event listener to 'Edit' buttons
            editButtons.forEach(function (button) {
                button.addEventListener("click", function () {
                    var x0 = button.getAttribute("data-x0");
                    var x1 = button.getAttribute("data-x1");
                    var x2 = button.getAttribute("data-x2");
                    var x3 = button.getAttribute("data-x3");
                    var x6 = button.getAttribute("data-x6");
                    var x8 = button.getAttribute("data-x8");

                    // Call the editfunc with the values from data attributes
                    editfunc(x0, x1, x2, x3, x6, x8);
                });
            });

            // Add event listener to 'Delete' buttons
            deleteButtons.forEach(function (button) {
                button.addEventListener("click", function () {
                    var x4 = button.getAttribute("data-x4");
                    var x1 = button.getAttribute("data-x1");

                    // Call the removeparticipantfunc with the values from data attributes
                    removeparticipantfunc(x4, x1);
                });
            });
        }
    );
}

function resetform() {
    $("#username").prop("readonly", false);
    $("#usermobile").prop("readonly", false);
    $("#statecode").prop("disabled", false);
    $("#emailid").val("");
    $("#usercode").val("");
    $("#username").val("");
    $("#usermobile").val("");
    $("#statecode").val("");
}

function checkmaterialdoctype() {
    if (document.getElementById("file1").files.length !== 0) {
        var mext = $("#file1").val().split(".").pop();
        if (
            mext != "jpg" &&
            mext != "JPG" &&
            mext != "jpeg" &&
            mext != "JPEG" &&
            mext != "pdf" &&
            mext != "PDF"
        ) {
            alert("Approval Letter should be of type jpg/jpeg/pdf only ");
            $("#file1").val("");
            $("#file1").focus();
            return false;
        }
    }
}

function addparticipantfunc() {
    $("#phaseid").val($("#phaseno").val());
}

function editfunc(ucode, pname, email, mno, scode, phid) {
    $("#emailid").val(email);
    $("#usercode").val(ucode);
    $("#username").val(pname);
    $("#usermobile").val(mno);
    $("#statecode").val(scode);
    $("#phaseid").val(phid);
}

function removeparticipantfunc(acode, pname) {
    if (
        confirm(
            "Are you sure you want to remove" +
                pname +
                " from the Program application list?"
        )
    ) {
        $.ajax({
            type: "POST",
            url: "/nerie/participant/remove",
            data: "applicationcode=" + acode,
            success: function (data) {
                if (data == "1") {
                    alert("Participant removed successfully");
                    getparticipantsfunc();
                } else {
                    alert("Error Occured!!! Please try again");
                    window.location.reload()
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    }
}
