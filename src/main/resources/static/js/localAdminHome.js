$(document).ready(function () {
    if ($("#calendar").length) {
        $("#calendar").MEC();
    }
    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });
    if ($('#completedTable').length) {
        $('#completedTable').DataTable({ responsive: false });
    }
    if ($('#closedTable').length) {
        $('#closedTable').DataTable({ responsive: false });
    }
    if ($('#upcomingTable').length) {
        $('#upcomingTable').DataTable({ responsive: false });
    }
    if ($('#ongoingTable').length) {
        $('#ongoingTable').DataTable({ responsive: false });
    }
    var st = $('#sysdate').val();
    var tzo = $('#timeZoneOffset').val();
    var jsVarServerTime;
    if (st) {
        jsVarServerTime = parseInt(st);
        function myTimerInternal() {
            jsVarServerTime = jsVarServerTime + 1000;
            var d = new Date(jsVarServerTime);
            var day = String(d.getDate()).padStart(2, '0');
            var month = String(d.getMonth() + 1).padStart(2, '0');
            var year = d.getFullYear();
            var sd = day + '/' + month + '/' + year;
            var hh = d.getHours();
            var mm = String(d.getMinutes()).padStart(2, '0');
            var ss = String(d.getSeconds()).padStart(2, '0');
            var ampm = (hh >= 12) ? "PM" : "AM";
            if (hh > 12) { hh -= 12; }
            else if (hh === 0) { hh = 12; }
            hh = String(hh).padStart(2, '0');
            if ($('#servdate').length) $('#servdate').html(sd);
            if ($('#servtime').length) $('#servtime').html(hh + ":" + mm + ":" + ss + " " + ampm);
        }
        myTimerInternal();
        setInterval(myTimerInternal, 1000);
    }
    if ($("#attendanceChart").length) {
        $.ajax({
            url: './getmyhomepageattendance.htm',
            type: 'GET',
            success: function (data) {
                var labels = [], barDataValues = [], attpercent = [], totalClassesArray = [], totalPresentArray = [], totalAbsentArray = [];
                if (data && Array.isArray(data)) {
                    data.forEach(function (row) {
                        labels.push(row[0]); barDataValues.push(row[5]); totalClassesArray.push(row[4]);
                        totalPresentArray.push(row[2]); totalAbsentArray.push(row[3]); attpercent.push(row[5]);
                    });
                } else {}
                var colorarray = ['#C6EFCA', '#F0CCDC', '#84D2C5', '#E4C988', '#C27664', '#B05A7A', '#567189', '#AD8E70', '#C7BCA1', '#C69749', '#B4CDE6', '#829460', '#A084CF', '#F0CCDC', '#F0CCDC', '#F0CCDC', '#F0CCDC', '#F0CCDC', '#F0CCDC'];
                var chartData = {
                    labels: labels,
                    datasets: [{ label: 'Attendance Percentage', data: barDataValues, backgroundColor: colorarray.slice(0, labels.length), borderWidth: 1 }]
                };
                var ctx = document.getElementById("attendanceChart").getContext("2d");
                var pieOptions = {
                    legend: { display: false },
                    tooltips: {
                        callbacks: {
                            label: function (tooltipItem, data) {
                                var label = data.labels[tooltipItem.index] || '';
                                if (label) { label += ': '; }
                                if (attpercent[tooltipItem.index] !== undefined) {
                                    label += 'Percent: ' + attpercent[tooltipItem.index] + '% ';
                                    label += 'Present: ' + totalPresentArray[tooltipItem.index] + ', ';
                                    label += 'Absent: ' + totalAbsentArray[tooltipItem.index] + ', ';
                                    label += 'Total Classes: ' + totalClassesArray[tooltipItem.index];
                                } else { label += 'No data'; }
                                return label;
                            }
                        }
                    }
                };
                new Chart(ctx, { type: "bar", data: chartData, options: pieOptions });
            },
            error: function(jqXHR, textStatus, errorThrown) {
            }
        });
    }
});

function getPhasesList(programDbId) {
    if (!programDbId) {
        $.alert("Error: Program DB ID is missing for getPhasesList.");
        return;
    }
    $.confirm({
        content: function () {
            var self = this;
            return $.ajax({
                url: './nerie/program-details/getAllProgramDetailsBasedOnProgramCode',
                data: 'programcode=' + programDbId,
                method: 'GET'
            }).done(function (r) {
                if(self && typeof self.close === 'function') self.close();
                if (r && r[0]) {
                    $('#programDetailsModal').modal('show');
                    var programData = r[0];
                    $("#officeName").html((programData.moffices && programData.moffices.officename) ? programData.moffices.officename : "N/A");
                    $("#programTitle").html(programData.programname || "N/A");
                    $("#programDesc").html(programData.programdescription || "N/A");
                    $(".programID").html(programData.programid || "N/A");
                    $("#programStatus").html(typeof programData.closed !== 'undefined' ? (programData.closed === 'N' ? 'Not Closed' : (programData.closed === 'Y' ? 'Closed' : "N/A")) : "N/A");
                    $("#programCategory").html((programData.mcoursecategories && programData.mcoursecategories.coursecategoryname) ? programData.mcoursecategories.coursecategoryname : "N/A");
                    var phaseListData = r[1];
                    var tc = '';
                    if (phaseListData && Array.isArray(phaseListData) && phaseListData.length > 0) {
                        phaseListData.forEach(function(phaseItem) {
                            var stat = 'Phase is Not Finalized';
                            if (phaseItem.finalized === 'Y') {
                                stat = phaseItem.closed === 'Y' ? 'Phase is Finalized and it is Closed' : 'Phase is Finalized and it is Ongoing';
                            }
                            tc += '<tr><td>' + (phaseItem.phase ? (phaseItem.phase.phaseno || 'N/A') : 'N/A') + '</td>' +
                                  '<td>' + (phaseItem.phase ? (phaseItem.phase.phasedescription || 'N/A') : 'N/A') + '</td>' +
                                  '<td>' + (phaseItem.startDate || 'N/A') + '</td><td>' + (phaseItem.endDate || 'N/A') + '</td>' +
                                  '<td>' + (phaseItem.coordinator || 'N/A') + '</td><td>' + stat + '</td></tr>';
                        });
                    } else { tc = '<tr><td colspan="6">No phases found.</td></tr>'; }
                    $("#phaseList").html(tc);
                    $("#programDetailsModal #printBTN").attr('href', 'printProgramDetails.htm?programcode=' + programDbId);
                } else {
                    $('#programDetailsModal').modal('hide'); $.alert("Error: Could not load program details.");
                }
            }).fail(function (jqXHR, textStatus, errorThrown) {
                if(self && typeof self.close === 'function') self.close();
                $.alert("Error fetching program details: " + textStatus);
            });
        }
    });
}

function getPhasesListDetails(programDbId) {
    if (!programDbId) { $.alert("Error: Program DB ID is missing."); return; }
    $.confirm({
        content: function () {
            var self = this;
            return $.ajax({
                url: './program/getAllProgramDetails',
                data: 'programcode=' + programDbId,
                method: 'GET'
            }).done(function (r) {
                if(self && typeof self.close === 'function') self.close();
                $('#MyProgramDetailsModal').modal('show');
                if (r && r[0]) {
                    var programData = r[0];
                    $("#officeNameDetailsModal").html((programData.moffices && typeof programData.moffices.officename !== 'undefined') ? programData.moffices.officename : "N/A");
                    $("#programTitleDetailsModal").html(programData.programname || "N/A");
                    $("#programDescDetailsModal").html(programData.programdescription || "N/A");
                    $(".programIDDetailsModal").html(programData.programid || "N/A");
                    $("#programStatusDetailsModal").html(typeof programData.closed !== 'undefined' ? (programData.closed === 'N' ? 'Not Closed' : (programData.closed === 'Y' ? 'Closed' : 'N/A')) : "N/A");
                    $("#programCategoryDetailsModal").html((programData.mcoursecategories && programData.mcoursecategories.coursecategoryname) ? programData.mcoursecategories.coursecategoryname : "N/A");
                    var phaseListData = r[1];
                    var tc = '';
                    if (phaseListData && Array.isArray(phaseListData) && phaseListData.length > 0) {
                        phaseListData.forEach(function(phaseItem) {
                            var stat = 'Phase is Not Finalized';
                            if (phaseItem.finalized === 'Y') stat = phaseItem.closed === 'Y' ? 'Phase Finalized & Closed' : 'Phase Finalized & Ongoing';
                            var phaseIdForButton = phaseItem.phase ? (phaseItem.phase.phaseid || '') : '';
                            tc += '<tr><td>' + (phaseItem.phase ? (phaseItem.phase.phaseno || 'N/A') : 'N/A') + '</td>' +
                                  '<td>' + (phaseItem.phase ? (phaseItem.phase.phasedescription || 'N/A') : 'N/A') + '</td>' +
                                  '<td>' + (phaseItem.startDate || 'N/A') + ' to ' + (phaseItem.endDate || 'N/A') + '</td>';
                            let venueNames = 'No Venues', RPNames = 'No Resource Persons';
                            if (phaseItem.VenuesAndRP && phaseItem.VenuesAndRP.length > 0) {
                                venueNames = ''; RPNames = '';
                                phaseItem.VenuesAndRP.forEach(function(vrp) { RPNames += (vrp.RPNames || 'N/A') + '<br>'; venueNames += (vrp.venueNames || 'N/A') + '<br>'; });
                            }
                            tc += '<td>' + venueNames + '</td><td>' + RPNames + '</td><td>' + (phaseItem.coordinator || 'N/A') + '</td><td>' + stat + '</td>' +
                                  '<td><button class="btn getmoredetails-btn btn-primary" data-phaseid="'+phaseIdForButton+'" data-programdbid="'+programDbId+'">More Details</button>' +
                                  '<button class="mt-3 btn getactivity-btn btn-primary" data-phaseid="'+phaseIdForButton+'" data-programdbid="'+programDbId+'">Activity Details</button></td></tr>';
                        });
                    } else { tc = '<tr><td colspan="8">No phases found.</td></tr>'; }
                    $("#phaseListDetailsModal").html(tc);
                    $("#MyProgramDetailsModal #printBTNDetailsModal").attr('href', '/nerie/reports/printProgramDetails?programcode=' + programDbId);
                    $('#phaseListDetailsModal .getmoredetails-btn').off('click').on('click', function(event) {
                        event.preventDefault(); var phaseid = $(this).data("phaseid"); var progDbId = $(this).data("programdbid");
                        if(phaseid && progDbId) getPhaseMoreDetails(phaseid, progDbId); else console.warn('Phase/ProgDB ID missing for More Details.');
                    });
                    $('#phaseListDetailsModal .getactivity-btn').off('click').on('click', function(event) {
                        event.preventDefault(); var phaseid = $(this).data("phaseid"); var progDbId = $(this).data("programdbid");
                        if(phaseid && progDbId) getActivities(phaseid, progDbId); else console.warn('Phase/ProgDB ID missing for Activity Details.');
                    });
                } else { $('#MyProgramDetailsModal').modal('hide'); $.alert("Error: Could not display program details."); }
            }).fail(function (jqXHR, textStatus, errorThrown) {
                if(self && typeof self.close === 'function') self.close();
                $('#MyProgramDetailsModal').modal('hide');
                $.alert("AJAX Error: " + textStatus);
            });
        }
    });
}

function getActivities(phaseid, programDbIdForPrint) {
    if (!phaseid) { $.alert("Error: Phase ID missing."); return; }
    $('#loader').show();
    $.ajax({
        url: './activities/getActivityBasedOnPhaseId', data: { phaseid: phaseid }, method: 'GET',
        success: function (response) {
            $('#loader').hide();
            var tableContent = '', programInfoFromServer = null;
            if (Array.isArray(response) && response.length > 0 && response[0] && response[0].programcode) {
                programInfoFromServer = response[0].programcode;
                $('#activityprogramname').text(programInfoFromServer.programname || 'N/A');
                $("#activityofficeNameDetailsModal").text((programInfoFromServer.moffices && programInfoFromServer.moffices.officename) ? programInfoFromServer.moffices.officename : 'N/A');
                $("#activityprogramID").text(programInfoFromServer.programid || 'N/A');
                let programStatusText = 'N/A';
                if (typeof programInfoFromServer.closed !== 'undefined') {
                    programStatusText = (programInfoFromServer.closed === 'Y' || programInfoFromServer.closed === true) ? 'Closed' : ((programInfoFromServer.closed === 'N' || programInfoFromServer.closed === false) ? 'Active' : 'N/A');
                }
                $("#activityprogramStatusDetailsModal").text(programStatusText);
                $("#activityprogramCategoryDetailsModal").text((programInfoFromServer.mcoursecategories && programInfoFromServer.mcoursecategories.coursecategoryname) ? programInfoFromServer.mcoursecategories.coursecategoryname : 'N/A');
            } else {
                $('#activityprogramname').text('N/A'); $("#activityofficeNameDetailsModal").text('N/A');
                $("#activityprogramID").text('N/A'); $("#activityprogramStatusDetailsModal").text('N/A');
                $("#activityprogramCategoryDetailsModal").text('N/A');
            }
            if (Array.isArray(response) && response.length > 0) {
                response.forEach(function (activity, index) {
                    tableContent += '<tr><td>' + (index + 1) + '</td>' +
                                    '<td>' + (activity.activityname || 'N/A') + '</td>' +
                                    '<td>' + (activity.activitydescription || 'N/A') + '</td>' +
                                    '<td>' + (formatDate(activity.activitystartdate) + (activity.activityenddate ? (' - ' + formatDate(activity.activityenddate)) : '')) + '</td>' +
                                    '<td>' + formatIndianExpenditure(activity.expenditure) + (activity.expenditure ? '/-' : '') + '</td></tr>';
                });
            } else { tableContent = '<tr><td colspan="5" class="text-center">No activities found for this phase.</td></tr>'; }
            $('#activityListDetailsModal').html(tableContent);
            $('#ActivitiesModal').modal('show');
            var printButtonInActivities = $('#ActivitiesModal').find('#printBTNDetailsModal');
            if(printButtonInActivities.length > 0) {
                 var printLink = programDbIdForPrint ?
                               '/nerie/reports/printProgramDetails?programcode=' + programDbIdForPrint + '&phaseid=' + phaseid :
                               '/nerie/reports/printProgramDetails?phaseid=' + phaseid;
                 printButtonInActivities.attr('href', printLink);
            }
        },
        error: function (xhr) {
            $('#loader').hide();
            $.alert(xhr.status === 404 ? "No activities found." : "Error fetching activities.");
        }
    });
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        var date = new Date(dateString);
        return isNaN(date.getTime()) ? 'Invalid Date' : date.toLocaleDateString('en-GB');
    } catch (e) {
        return 'Invalid Date String';
    }
}

function formatIndianExpenditure(expenditureString) {
    if (expenditureString === null || typeof expenditureString === 'undefined') return 'N/A';
    let numStr = String(expenditureString);
    if (numStr.includes(',')) return numStr;
    return numStr.replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1,');
}

function getPhaseMoreDetails(phaseid, programDbIdForPrint) {
    if (!phaseid) { $.alert("Error: Phase ID missing."); return; }
    $('#loader').show();
    $.ajax({
        url: './phase-more-details/getPhaseMoreDetailsBasedOnPhaseId', data: { phaseid: phaseid }, method: 'GET',
        success: function (response) {
            $('#loader').hide();
            if (!response) { $.alert("Empty data for phase details."); $('#PhaseMoreDetailsModal').modal('hide'); return; }
            var programInfoFromServer = response.programcode;
            if (programInfoFromServer) {
                $('#PhaseMoreDetailsprogramname').text(programInfoFromServer.programname || 'N/A');
                $("#PhaseMoreDetailsofficeNameDetailsModal").text((programInfoFromServer.moffices && programInfoFromServer.moffices.officename) ? programInfoFromServer.moffices.officename : 'N/A');
                $('#PhaseMoreDetailsprogramID').text(programInfoFromServer.programid || 'N/A');
                let programStatusText = 'N/A';
                if (typeof programInfoFromServer.closed !== 'undefined') {
                    programStatusText = (programInfoFromServer.closed === 'Y' || programInfoFromServer.closed === true) ? 'Program Closed' : ((programInfoFromServer.closed === 'N' || programInfoFromServer.closed === false) ? 'Program Active' : 'N/A');
                }
                $('#PhaseMoreDetailsprogramStatusDetailsModal').text(programStatusText);
                $("#PhaseMoreDetailsprogramCategoryDetailsModal").text((programInfoFromServer.mcoursecategories && programInfoFromServer.mcoursecategories.coursecategoryname) ? programInfoFromServer.mcoursecategories.coursecategoryname : 'N/A');
            } else {
                $('#PhaseMoreDetailsprogramname').text('N/A'); $("#PhaseMoreDetailsofficeNameDetailsModal").text('N/A');
                $('#PhaseMoreDetailsprogramID').text('N/A'); $('#PhaseMoreDetailsprogramStatusDetailsModal').text('N/A');
                $('#PhaseMoreDetailsprogramCategoryDetailsModal').text('N/A');
            }
            $("#PhaseMoreDetailsFocusAreas").text(response.focusareas || "N/A");
            $("#PhaseMoreDetailsBudgetProposed").text(response.budgetproposed || "N/A");
            $("#PhaseMoreDetailsTargetGroup").text(response.targetgroup || "N/A");
            $("#PhaseMoreDetailsStage").text(response.stage || "N/A");
            $("#PhaseMoreDetailsObjectives").text(response.objectives || "N/A");
            $("#PhaseMoreDetailsMethodology").text(response.methodology || "N/A");
            $("#PhaseMoreDetailsTools").text(response.tools || "N/A");
            $("#PhaseMoreDetailsKPIndicators").text(response.kpindicators || "N/A");
            $("#PhaseMoreDetailsOutcomes").text(response.outcomes || "N/A");
            $('#PhaseMoreDetailsModal').modal('show');
            var printButtonInPhaseMore = $('#PhaseMoreDetailsModal').find('#printBTNDetailsModal');
            if(printButtonInPhaseMore.length > 0) {
                 var printLink = programDbIdForPrint ?
                               '/nerie/reports/printProgramDetails?programcode=' + programDbIdForPrint + '&phaseid=' + phaseid :
                               '/nerie/reports/printProgramDetails?phaseid=' + phaseid;
                 printButtonInPhaseMore.attr('href', printLink);
            }
        },
        error: function (xhr) {
            $('#loader').hide();
            var alertMsg = "Error fetching phase details.";
            if (xhr.status === 404) { alertMsg = "Phase details not found."; }
            else if (xhr.status === 500) { alertMsg = "Server error fetching phase details."; }
            $.alert(alertMsg); $('#PhaseMoreDetailsModal').modal('hide');
        }
    });
}

$('.sub-menu ul').hide(); $('.sub-sub-menu ul').hide();
$(".sub-menu a").click(function () { $(this).parent(".sub-menu").children("ul").slideToggle(100); $(this).find(".right").toggleClass("fa-caret-up fa-caret-down"); });
$(".sub-sub-menu a").click(function () { $(this).parent(".sub-sub-menu").children("ul").slideToggle(100); $(this).find(".right").toggleClass("fa-caret-up fa-caret-down"); });

$(function () {
    $("#one-item-row").on("click", function () { $(".b-customize").removeClass("col-lg-3 col-lg-4 col-lg-6").addClass("col-lg-12", 300); });
    $("#two-item-row").on("click", function () { $(".b-customize").removeClass("col-lg-6 col-lg-12").addClass("col-lg-6", 300); });
    $("#three-item-row").on("click", function () { $(".b-customize").removeClass("col-lg-6 col-lg-12").addClass("col-lg-3", 300); });
});

document.addEventListener('DOMContentLoaded', function () {
    var programDetailTriggerButtons = document.querySelectorAll('button[data-program-code]:not([data-phaseid])');
    programDetailTriggerButtons.forEach(function(button) {
        let isForMainDetails = ['myprogramsbtn', 'ongoingbtn', 'upcomingbtn', 'closedbtn', 'completedbtn', 'archivedbtn'].some(cls => button.classList.contains(cls));
        if (isForMainDetails) {
            button.addEventListener("click", function(event) {
                event.preventDefault();
                var programDbId = button.getAttribute("data-program-code");
                if (programDbId) { getPhasesListDetails(programDbId); }
            });
        }
    });
});