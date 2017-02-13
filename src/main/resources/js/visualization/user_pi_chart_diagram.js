"use strict";

function appendTimeToPiChart(sortedDataArray, numberOfCategories) {
    var data = [];
    for (var i = 0; i < numberOfCategories; i++) {
        if (sortedDataArray[i][0] == "Inactive" || sortedDataArray[i][0] == "Inactive & Offline") {
            continue;
        }
        var sum = 0;
        for (var k = 2; k < sortedDataArray[i].length; k+=2) {
            sum += sortedDataArray[i][k];
        }

        data.push({
            label: sortedDataArray[i][0],
            value: toFixed(sum, 2)
        });
    }

    var piChart = drawPiChart(data);

    AJS.$("#table-header > ul > li > a").bind("tabSelect", function(e, o) {
        if (o.tab.attr("href") == "#tabs-overview") {
            piChart.redraw();
        }
    });
}