"use strict";

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