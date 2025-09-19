$(document).ready(function () {
    $.ajax({
        type: "GET",
        url: "/nerie/students/info",
        data: "",
        success: function (data) {
            if (data) {
                var studentArr = JSON.parse(data.student);
                $("#inputAcademicYear").val(studentArr.academicyear);
                $("#inputStudentid").val(studentArr.studentid);
                studentArr.dateofbirth? $("#inputDOB").val(studentArr.dateofbirth) : $("#inputDOB").val("Not Provided");
                //                                $("#inputFname").val(studentArr.fname);
                //                                $("#inputMname").val(studentArr.mname);
                //                                $("#inputLname").val(studentArr.lname);
                $("#inputName").val(studentArr.fname +' '+ studentArr.mname+' ' + studentArr.lname)
                $("#inputMobileno").val(studentArr.mobileno);
                $("#inputEmail").val(studentArr.email);
                $("#inputDepartment").val(studentArr.departmentcode.departmentname);
                $("#inputCourse").val(studentArr.coursecode.coursename);

                if (studentArr && studentArr.sphaseid && studentArr.sphaseid.sphasename) {
                    // If sphasename exists, set the input value to sphasename
                    $("#inputSemphase").val(studentArr.sphaseid.sphasename);
                } else if (studentArr && studentArr.semestercode && studentArr.semestercode.semestername) {
                    // If semestername exists, set the input value to semestername
                    $("#inputSemphase").val(studentArr.semestercode.semestername);
                }
                //$("#inputSemphase").val(studentArr.semestercode.semestername);
                $("#inputName").val(studentArr.fname + " " + studentArr.mname + " " + studentArr.lname);
            } else {
                alert("Error Occured!!! Please try again");
            }
        },
        error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
    });

    $(document).ready(function () {
        $("#uploadbtn").click(function (event) {
            event.preventDefault();

            $.confirm({
                content: function () {
                    var self = this;
                    return $.ajax({
                        type: "POST",
                        url: "/nerie/users/saveprofilephoto",
                        data: new FormData($("#uploadphotoform")[0]),
                        enctype: 'multipart/form-data',
                        processData: false,
                        contentType: false,
                    }).done(function (data) {
                        if (data === '1') {
                            self.setContent('Profile photo has been uploaded');
                            self.setTitle('Success');
                        } else {
                            self.setTitle('Failed to Upload Photo');
                        }
                    }).fail(jqXHR => {
                        if (jqXHR.status === 401)
                                window.location.href = '/nerie/login'
                        self.setContent('Something went wrong.');
                    });
                },
                onDestroy: function () {
                    window.location.reload();
                }
            });
        });
    });


    $(document).on('change', 'input[type="file"]', function () {
        filesize = this.files[0].size / 1024;
        if (filesize > 500)
        {
            alert('Your Filesize is ' + filesize + ' kb \n Filesize cannot be greater than 500 Kb');
            this.form.reset();
        }
    });
});

function previewFile1image() {
    if (document.getElementById("file1").files.length !== 0) {
        var mext = $("#file1").val().split('.').pop();
        //                                        var msize = $('#pimgfiles')[0].files[0].size;
        if (((mext === "jpg") || (mext === "JPG") || (mext === "jpeg") || (mext === "JPEG") || (mext === "png") || (mext === "PNG"))) {
            var file = $('#file1')[0].files[0];
            if (file) {
                var reader = new FileReader();
                reader.onload = function () {
                    $("#previewfile1").attr("src", reader.result);
                }
                reader.readAsDataURL(file);
            }
        } else {
            $("#file1").val("");
            $("#file1").focus();
            alert("Photo should be of type jpg/png");
            return false;
        }
    }
}
