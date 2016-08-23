"use strict";

var restBaseUrl;

AJS.toInit(function () {
    var baseUrl = AJS.params.baseURL;
    restBaseUrl = baseUrl + "/rest/timesheet/latest/";
    AJS.$("#timesheet-export-csv-link").empty();

    var isValid = checkContrains();

    if (isValid == "true") {
        console.log("User is valid!");
    }
    else {
        console.log("User is not Valid!");
        AJS.$("#timesheet-table").hide();
        AJS.$(".tabs-pane").hide();
        AJS.$("#table-header").hide();

        AJS.messages.error({
            title: 'Sorry, you have no timesheet',
            body: 'You are not authorized to view this page. Please contact the administrator! </br> ' +
            'Probably you are not added to a team or category!'
        });
        return;
    }

    if (isMasterThesisTimesheet) {
        document.getElementById("tabs-timesheet-settings").style.display = "none";
        document.getElementById("tabs-team").style.display = "none";
        AJS.$("#timesheet-export-csv-link").append("<h2>Export</h2>Download 'Master Thesis Timesheet' as <a href=\"download/masterthesis\">CSV</a>.");
    } else {
        AJS.$("#timesheet-export-csv-link").append("<h2>Export</h2>Download 'Timesheet' as <a href=\"download/timesheet\">CSV</a>.");
    }

    AJS.$("#tabs-team").submit(function (e) {
        e.preventDefault();
        if (AJS.$(document.activeElement).val() === 'Visualize') {
            var selectedTeam = AJS.$("#select-team-select2-field").val().split(',');
            if (selectedTeam[0] !== "") {
                getDataOfTeam(selectedTeam[0]);
            }
        }
    });

    if (isAdmin) {
        hideVisualizationTabs();
        fetchUsers();
        AJS.$("#timesheet-hours-save-button").hide();
        AJS.$("#timesheet-hours-update-button").show();
    } else {
        //init coordinator/administrator/approved user Seetings
        initUserSaveButton();
        //fetch timesheet table data
        fetchUsers();
        fetchData();
        //fetch visualization data
        fetchVisData();
        fetchTeamVisData();
        AJS.$("#timesheet-hours-save-button").show();
        AJS.$("#timesheet-hours-update-button").hide();
    }
});

function checkContrains() {
    return AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'checkConstrains',
        async: false
    }).responseText;
}

function fetchUserTimesheetData(timesheetID) {

    var timesheetFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheets/' + timesheetID,
        contentType: "application/json"
    });

    var entriesFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheets/' + timesheetID + '/entries',
        contentType: "application/json"
    });

    var categoriesFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'categories',
        contentType: "application/json"
    });

    var teamsFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'teams/' + timesheetID,
        contentType: "application/json"
    });
    var usersFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'user/getUsers',
        contentType: "application/json"
    });
    AJS.$.when(timesheetFetched, categoriesFetched, teamsFetched, entriesFetched, usersFetched)
        .done(assembleTimesheetData)
        .done(populateTable, prepareImportDialog)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function fetchUserVisData(timesheetID) {
    var timesheetFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheets/' + timesheetID,
        contentType: "application/json"
    });

    var entriesFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheets/' + timesheetID + '/entries',
        contentType: "application/json"
    });

    var categoriesFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'categories',
        contentType: "application/json"
    });

    var teamsFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'teams',
        contentType: "application/json"
    });
    AJS.$.when(timesheetFetched, categoriesFetched, teamsFetched, entriesFetched)
        .done(assembleTimesheetVisData)
        .done(populateVisTable)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function getDataOfTeam(teamName) {
    var teamData = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheet/' + teamName + '/entries',
        contentType: "application/json"
    });
    AJS.$.when(teamData)
        .done(assignTeamData)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching team data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function getTimesheetOfUser(selectedUser, isMTSheet) {
    var timesheetIDFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheet/timesheetID/' + selectedUser[0] + '/' + isMTSheet,
        contentType: "application/json"
    });
    AJS.$.when(timesheetIDFetched)
        .done(fetchUserTimesheetData)
        .done(fetchUserVisData)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while getting timesheet data of another user.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function getTimesheetByUser(selectedUser, isMTSheetSelected) {
    var timesheetFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheet/of/' + selectedUser + '/' + isMTSheetSelected,
        contentType: "applicatPion/json"
    });
    AJS.$.when(timesheetFetched)
        .done(updateTimesheetHours)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching existing timesheet data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function getExistingTimesheetHours(timesheetID) {
    var timesheetFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheets/' + timesheetID,
        contentType: "applicatPion/json"
    });
    AJS.$.when(timesheetFetched)
        .done(updateTimesheetHours)
        .done(location.reload())
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching existing timesheet data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function updateTimesheetHours(existingTimesheetData) {
    var timesheetUpdateData = {
        timesheetID: existingTimesheetData.timesheetID,
        lectures: AJS.$("#timesheet-hours-lectures").val(),
        reason: AJS.$("#timesheet-substract-hours-text").val(),
        ects: AJS.$("#timesheet-hours-ects").val(),
        targetHourPractice: toFixed(AJS.$("#timesheet-hours-practical").val(), 2),
        targetHourTheory: toFixed(AJS.$("#timesheet-hours-theory").val(), 2),
        targetHours: AJS.$("#timesheet-hours-ects").val() * 30,
        targetHoursCompleted: toFixed((AJS.$("#timesheet-hours-theory").val()
        - (-AJS.$("#timesheet-hours-practical").val()) - AJS.$("#timesheet-hours-substract").val()), 2),
        targetHoursRemoved: toFixed(AJS.$("#timesheet-hours-substract").val(), 2),
        isActive: existingTimesheetData.isActive,
        isEnabled: existingTimesheetData.isEnabled,
        isMTSheet: existingTimesheetData.isMTSheet
    };

    AJS.$.ajax({
        type: 'POST',
        url: restBaseUrl + 'timesheets/update/' + existingTimesheetData.timesheetID + '/' + existingTimesheetData.isMTSheet,
        contentType: "application/json",
        data: JSON.stringify(timesheetUpdateData)
    })
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while updating the timesheet data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
        });
}

function fetchUsers() {
    var config = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'config/getConfig',
        contentType: "application/json"
    });

    var jsonUser = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheets/owner/' + timesheetID,
        contentType: "application/json"
    });

    var userList = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'user/getUsers',
        contentType: "application/json"
    });
    AJS.$.when(config, jsonUser, userList)
        .done(initCoordinatorTimesheetSelect)
        .done(initApprovedUserTimesheetSelect)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching user data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function fetchData() {
    var timesheetFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheets/' + timesheetID,
        contentType: "application/json"
    });

    var entriesFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheets/' + timesheetID + '/entries',
        contentType: "application/json"
    });

    var categoriesFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'categories',
        contentType: "application/json"
    });

    var teamsFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'teams',
        contentType: "application/json"
    });
    var usersFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'user/getUsers',
        contentType: "application/json"
    });
    AJS.$.when(timesheetFetched, categoriesFetched, teamsFetched, entriesFetched, usersFetched)
        .done(assembleTimesheetData)
        .done(populateTable, prepareImportDialog)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching timesheet data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}
