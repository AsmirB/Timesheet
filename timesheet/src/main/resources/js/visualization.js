"use strict";

//var baseUrl, visualizationTable, timesheetForm, restBaseUrl;

function fetchVisData() {
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
        .done(computeCategoryDiagramData)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function fetchTeamVisData() {
    var timesheetFetched = AJS.$.ajax({
        type: 'GET',
        url: restBaseUrl + 'timesheet/' + timesheetID + '/teamEntries',
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
        .done(assignTeamVisData)
        .fail(function (error) {
            AJS.messages.error({
                title: 'There was an error while fetching data.',
                body: '<p>Reason: ' + error.responseText + '</p>'
            });
            console.log(error);
        });
}

function assembleTimesheetVisData(timesheetReply, categoriesReply, teamsReply, entriesReply) {
    var timesheetData = timesheetReply[0];

    timesheetData.entries = entriesReply[0];
    timesheetData.categories = [];
    timesheetData.teams = [];

    categoriesReply[0].map(function (category) {
        timesheetData.categories[category.categoryID] = {
            categoryName: category.categoryName
        };
    });

    teamsReply[0].map(function (team) {
        timesheetData.teams[team.teamID] = {
            teamName: team.teamName,
            teamCategories: team.teamCategories
        };
    });

    return timesheetData;
}

function assignTeamData(entries) {
    AJS.$("#teamDataDiagram").empty();
    var availableEntries = entries;

    var pos = availableEntries.length - 1;
    //variables for the time calculation
    var totalHours = 0;
    var totalMinutes = 0;

    //data array
    var dataPoints = [];

    for (var i = availableEntries.length - 1; i >= 0; i--) {
        //calculate spent time for team
        var referenceEntryDate = new Date(availableEntries[pos].beginDate);
        var compareToDate = new Date(availableEntries[i].beginDate);
        var oldPos = pos;

        if ((referenceEntryDate.getFullYear() == compareToDate.getFullYear()) &&
            (referenceEntryDate.getMonth() == compareToDate.getMonth())) {
            //add all times for the same year-month pairs
            var hours = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
                availableEntries[i].pauseMinutes).getHours();
            var minutes = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
                availableEntries[i].pauseMinutes).getMinutes();
            var pause = availableEntries[i].pauseMinutes;
            var calculatedTime = hours * 60 + minutes - pause;

            totalMinutes = totalMinutes + calculatedTime;

            if (totalMinutes >= 60) {
                var minutesToFullHours = Math.floor(totalMinutes / 60); //get only full hours
                totalHours = totalHours + minutesToFullHours;
                totalMinutes = totalMinutes - minutesToFullHours * 60;
            }
        } else {
            pos = i;
            i = i + 1;
        }

        if (oldPos != pos || i == 0) {
            var dataX = referenceEntryDate.getFullYear() + "-" + (referenceEntryDate.getMonth() + 1);
            var dataY = totalHours + totalMinutes / 60;
            dataPoints.push(dataX);
            dataPoints.push(dataY);
            totalHours = 0;
            totalMinutes = 0;
        }
    }

    //assign JSON data for line graph
    teamDiagram(dataPoints);
}

function assignTeamVisData(timesheetDataReply) {
    var availableEntries = timesheetDataReply[0].entries;
    var availableTeams = timesheetDataReply[0].teams;

    var pos = 0;
    //variables for the time calculation
    var totalHours = 0;
    var totalMinutes = 0;

    //data array
    var data = {};
    data['label'] = [];
    data['year'] = [];
    data['team'] = [];

    for (var i = 0; i < availableEntries.length; i++) {
        //calculate spent time for team
        var referenceEntryDate = new Date(availableEntries[pos].beginDate);
        var compareToDate = new Date(availableEntries[i].beginDate);
        var actualTeamID = availableEntries[pos].teamID;
        var oldPos = pos;


        if ((referenceEntryDate.getFullYear() == compareToDate.getFullYear()) &&
            (referenceEntryDate.getMonth() == compareToDate.getMonth()) &&
            (availableEntries[i].teamID == actualTeamID)) {
            //add all times for the same year-month pairs
            var hours = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
                availableEntries[i].pauseMinutes).getHours();
            var minutes = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
                availableEntries[i].pauseMinutes).getMinutes();
            var pause = availableEntries[i].pauseMinutes;
            var calculatedTime = hours * 60 + minutes - pause;

            totalMinutes = totalMinutes + calculatedTime;

            if (totalMinutes >= 60) {
                var minutesToFullHours = Math.floor(totalMinutes / 60); //get only full hours
                totalHours = totalHours + minutesToFullHours;
                totalMinutes = totalMinutes - minutesToFullHours * 60;
            }

        } else {
            pos = i;
            i = i - 1;
        }

        if (oldPos != pos || i == availableEntries.length - 1) {
            data['year'].push(referenceEntryDate.getFullYear() + "-" + (referenceEntryDate.getMonth() + 1));
            data['team'].push((totalHours + totalMinutes / 60));
            if (!containsElement(data['label'], availableTeams[actualTeamID].teamName))
                data['label'].push(availableTeams[actualTeamID].teamName);
            totalHours = 0;
            totalMinutes = 0;
        }
    }

    var temp = []
    //build data JSON object (year is represented on the x-axis; time on the y-axis
    for (var i = 0; i < data['year'].length; i++) {
        temp.push(data['year'][i]);
        temp.push(data['label'][i]);
        temp.push(data['team'][i]);
    }

    var dataJSON = [];
    for (var i = 0; i < temp.length; i = i + 3) {
        if (i % 2 == 0)
            dataJSON.push({
                year: temp[i],
                team1: temp[i + 2],
                team2: 0
            });
        else
            dataJSON.push({
                year: temp[i],
                team1: 0,
                team2: temp[i + 2]
            });
    }

    drawTeamDiagram(dataJSON, data['label']);
}

function populateVisTable(timesheetDataReply) {
    var timesheetData = timesheetDataReply[0];
    AJS.$("#visualization-table-content").empty();
    appendEntriesToVisTable(timesheetData);
}

function computeCategoryDiagramData(timesheetDataReply) {
    assignCategoryDiagramData(timesheetDataReply[0]);
}

function appendTimeToPiChart(theoryTime, practicalTime, totalTime) {
    var piChartDataPoints = [];

    //practice hours
    piChartDataPoints.push("Practice");
    piChartDataPoints.push(((practicalTime * 100) / totalTime).toString().slice(0, 5));
    //theory hours
    piChartDataPoints.push("Theory");
    piChartDataPoints.push(((theoryTime * 100) / totalTime).toString().slice(0, 5));

    drawPiChartDiagram(piChartDataPoints);
}

function appendEntriesToVisTable(timesheetData) {

    var availableEntries = timesheetData.entries;

    var pos = 0;
    var i = 0;
    //variables for the time calculation
    var totalHours = 0;
    var totalMinutes = 0;
    var totalTimeHours = 0;
    var totalTimeMinutes = 0;
    var timeLastSixMonthHours = 0;
    var timeLastSixMonthMinutes = 0;
    //save data in an additional array
    var count = 0;
    var dataPoints = [];
    //pi chart variables
    var theoryHours = 0;

    //spent time within the last six months
    var sixMonthAgo = new Date().getMonth() - 6;

    while (i < availableEntries.length) {
        var referenceEntryDate = new Date(availableEntries[pos].beginDate);
        var compareToDate = new Date(availableEntries[i].beginDate);
        var oldPos = pos;

        if ((referenceEntryDate.getFullYear() == compareToDate.getFullYear()) &&
            (referenceEntryDate.getMonth() == compareToDate.getMonth())) {
            //add all times for the same year-month pairs
            var hours = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
                availableEntries[i].pauseMinutes).getHours();
            var minutes = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
                availableEntries[i].pauseMinutes).getMinutes();
            var pause = availableEntries[i].pauseMinutes;
            var calculatedTime = hours * 60 + minutes - pause;

            totalMinutes = totalMinutes + calculatedTime;

            if (totalMinutes >= 60) {
                var minutesToFullHours = Math.floor(totalMinutes / 60); //get only full hours
                totalHours = totalHours + minutesToFullHours;
                totalMinutes = totalMinutes - minutesToFullHours * 60;
            }

            //calculate theory time in minutes
            if (timesheetData.categories[availableEntries[i].categoryID].categoryName === "Theory")
                theoryHours = theoryHours + calculatedTime;

            //date within the last six months
            var compareEntryMonth = compareToDate.getMonth() - 6;
            if (compareEntryMonth <= sixMonthAgo) {
                timeLastSixMonthHours = timeLastSixMonthHours + hours;
                timeLastSixMonthMinutes = timeLastSixMonthMinutes + minutes;
            }
        } else {
            pos = i;
            i = i - 1;
        }

        if (oldPos != pos || i == availableEntries.length - 1) {
            //add points to line diagram
            var dataX = referenceEntryDate.getFullYear() + "-" + (referenceEntryDate.getMonth() + 1);
            var dataY = totalHours + totalMinutes / 60;
            dataPoints.push(dataX);
            dataPoints.push(dataY);

            AJS.$("#visualization-table-content").append("<tr><td headers=\"basic-date\" class=\"date\">" +
                "Time Spent: " + referenceEntryDate.getFullYear() + "-" + (referenceEntryDate.getMonth() + 1) + "</td>" +
                "<td headers=\"basic-time\" class=\"time\">" + totalHours + "hours " + totalMinutes + "mins" + "</td>" +
                "</tr>");

            count++;

            //overall sum of spent time
            totalTimeHours = totalTimeHours + totalHours;
            totalTimeMinutes = totalTimeMinutes + totalMinutes;

            if (totalTimeMinutes >= 60) {
                var minutesToFullHours = Math.floor(totalTimeMinutes / 60); //get only full hours
                totalTimeHours = totalTimeHours + minutesToFullHours;
                totalTimeMinutes = totalTimeMinutes - minutesToFullHours * 60;
            }
            totalHours = 0;
            totalMinutes = 0;
        }

        i = i + 1;
    }

    var totalTime = totalTimeHours * 60 + totalTimeMinutes;

    //append total time
    AJS.$("#visualization-table-content").append("<tr><td headers=\"basic-date\" class=\"total-time\">" + "Total Spent Time" + "</td>" +
        "<td headers=\"basic-time\" class=\"time\">" + totalTimeHours + "hours " + totalTimeMinutes + "mins" + "</td>" +
        "</tr>");

    //entry for average time
    var averageMinutesPerMonth = (totalTimeHours * 60 + totalTimeMinutes) / count;
    var averageTimeHours = 0;
    var averageTimeMinutes = 0;

    if (averageMinutesPerMonth >= 60) {
        var minutesToFullHours = Math.floor(averageMinutesPerMonth / 60); //get only full hours
        averageTimeHours = minutesToFullHours;
        averageTimeMinutes = averageMinutesPerMonth - minutesToFullHours * 60;
    }

    //append avg time
    AJS.$("#visualization-table-content").append("<tr><td headers=\"basic-date\" class=\"avg-time\">" + "Time / Month" + "</td>" +
        "<td headers=\"basic-time\" class=\"time\">" + averageTimeHours + "hours " + averageTimeMinutes + "mins" + "</td>" +
        "</tr>");

    //append time last 6 month
    AJS.$("#visualization-table-content").append("<tr><td headers=\"basic-date\" class=\"total-time\">" + "Overall Time Last 6 Month" + "</td>" +
        "<td headers=\"basic-time\" class=\"time\">" + timeLastSixMonthHours + "hours " + timeLastSixMonthMinutes + "mins" + "</td>" +
        "</tr>");

    //assign JSON data for line graph
    diagram(dataPoints);
    appendTimeToPiChart(theoryHours, totalTime - theoryHours, totalTime);
}

//reverse order of the table from bottom to top
function assignCategoryDiagramData(timesheetData) {

    var availableEntries = timesheetData.entries;

    var pos = availableEntries.length - 1;
    var i = availableEntries.length - 1;
    //variables for the time calculation
    var totalHours = 0;
    var totalMinutes = 0;
    var totalTimeHours = 0;
    var totalTimeMinutes = 0;
    //save data in an additional array
    var index = 0;
    var dataPoints = [];

    while (i >= 0) {
        var referenceEntryDate = new Date(availableEntries[pos].beginDate);
        var compareToDate = new Date(availableEntries[i].beginDate);
        var oldPos = pos;

        if ((referenceEntryDate.getFullYear() === compareToDate.getFullYear()) &&
            (referenceEntryDate.getMonth() === compareToDate.getMonth())) {
            totalHours = 0;
            totalMinutes = 0;
            //add all times for the same year-month pairs
            var hours = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
                availableEntries[i].pauseMinutes).getHours();
            var minutes = calculateDuration(availableEntries[i].beginDate, availableEntries[i].endDate,
                availableEntries[i].pauseMinutes).getMinutes();
            var pause = availableEntries[i].pauseMinutes;
            var calculatedTime = hours * 60 + minutes - pause;

            totalMinutes = totalMinutes + calculatedTime;

            if (totalMinutes >= 60) {
                var minutesToFullHours = Math.floor(totalMinutes / 60); //get only full hours
                totalHours = totalHours + minutesToFullHours;
                totalMinutes = totalMinutes - minutesToFullHours * 60;
            }

            //add points
            var dataX = referenceEntryDate.getFullYear() + "-" + (referenceEntryDate.getMonth() + 1);
            var dataY = totalHours + totalMinutes / 60;
            dataPoints.push(dataX);
            dataPoints.push(dataY);
            dataPoints.push(timesheetData.categories[availableEntries[i].categoryID].categoryName);
        } else {
            pos = i;
            i = i + 1;
        }

        if (oldPos != pos || i === 0) {
            if (totalTimeMinutes >= 60) {
                var minutesToFullHours = Math.floor(totalTimeMinutes / 60); //get only full hours
                totalTimeHours = totalTimeHours + minutesToFullHours;
                totalTimeMinutes = totalTimeMinutes - minutesToFullHours * 60;
            }
        }

        i = i - 1;
    }

    var categories = [];
    //filter all category names
    for (var i = 0; i < dataPoints.length; i++)
        //read category name at position 3
        if (i % 3 === 2)
            if (!containsElement(categories, dataPoints[i]))
                categories.push(dataPoints[i]);


    var sortedDataArray = [];
    var tempArray = [];

    for (var k = 0; k < categories.length; k++) {
        for (var i = 0; i < dataPoints.length; i++) {
            //fill in category name at first pos of subarray
            if (i === 0) {
                tempArray.push(categories[k]);
            }

            //read category name at position 3
            if ((i % 3 === 2) && (dataPoints[i] === categories[k])) {
                tempArray.push(dataPoints[i - 2]);
                tempArray.push(dataPoints[i - 1]);
            }

            //add subarray to array and pick next category
            if (i === dataPoints.length - 1) {
                sortedDataArray.push(tempArray);
                tempArray = [];
            }
        }
    }

    categoryDiagram(sortedDataArray, categories.length)
}

function categoryDiagram(sortedDataArray, numberOfCategories) {
    var data = {};
    //create data json array dynamically
    data['label'] = [];
    data['year'] = [];
    for (var i = 0; i < numberOfCategories; i++) {
        //console.log(sortedDataArray[i]);
        //labels
        if (!containsElement(data['label'], sortedDataArray[i][0]))
            data['label'].push(sortedDataArray[i][0]);
        //years
        for (var j = 1; j < sortedDataArray[i].length - 1; j = j + 2)
            if (!containsElement(data['year'], sortedDataArray[i][j]))
                data['year'].push(sortedDataArray[i][j]);
        //values
        data['category' + i] = [];
        for (var l = 0; l < data['year'].length; l++) {
            var sum = 0;
            for (var k = 1; k < sortedDataArray[i].length; k++) {
                if (sortedDataArray[i][k] == data['year'][l])
                    sum = sum + sortedDataArray[i][k + 1];
            }
            data['category' + i].push(sum);
        }
    }

    var dataJSON = [];
    var tempData = [];
    //build data JSON object (year is represented on the x-axis; time on the y-axis
    for (var i = 0; i < data['year'].length; i++) {
        for (var key in data) {
            var obj = data[key];
            tempData.push(obj[i]);
        }
        //console.log(tempData);
        //fill JSON array
        dataJSON.push({
            year: tempData[1],
            cat1: tempData[2],
            cat2: tempData[3],
            cat3: tempData[4],
            cat4: tempData[5],
            cat5: tempData[6]
        });

        tempData = [];
    }
    drawCategoryDiagram(dataJSON, data['label']);
}

function diagram(dataPoints) {
    var data = [];
    for (var i = 0; i < dataPoints.length; i = i + 2) {
        data.push({
            year: dataPoints[i],
            value: dataPoints[i + 1]
        });
    }
    drawDiagram(data);
}

function drawPiChartDiagram(dataPoints) {
    var data = [];
    for (var i = 0; i < dataPoints.length; i = i + 2) {
        data.push({
            label: dataPoints[i],
            value: dataPoints[i + 1]
        });
    }
    drawPiChart(data);
}

function teamDiagram(dataPoints) {
    var data = [];
    for (var i = 0; i < dataPoints.length; i = i + 2) {
        data.push({
            year: dataPoints[i],
            value: dataPoints[i + 1]
        });
    }
    drawSelectedTeamDiagram(data);
}

function calculateDuration(begin, end, pause) {
    var pauseDate = new Date(pause);
    return new Date(end - begin - (pauseDate.getHours() * 60 + pauseDate.getMinutes()) * 60 * 1000);
}


function containsElement(array, ele) {
    for (var p in array)
        if (array[p] === ele)
            return true;
    return false;
}
