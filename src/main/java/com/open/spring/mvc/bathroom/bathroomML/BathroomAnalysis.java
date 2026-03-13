package com.open.spring.mvc.bathroom.bathroomML;

import java.io.InputStream;

import static tech.tablesaw.aggregate.AggregateFunctions.mean;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.Histogram;
import tech.tablesaw.plotly.api.VerticalBarPlot;

/*
 * Loads bathroom CSV and displays analyzed data in a chart
 */
public class BathroomAnalysis {
    public static void main(String[] args) throws Exception {
        InputStream inputStream = BathroomAnalysis.class.getResourceAsStream("/data/bathroom_cleaned.csv");

        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: bathroom_cleaned.csv");
        }
        Table bathroom = Table.read().csv(inputStream);

        // Analysis: Bathroom visits by student
        Table nameCounts = bathroom.countBy(bathroom.stringColumn("Name"));
        System.out.println("Bathroom visits per student:");
        System.out.println(nameCounts);
        Plot.show(VerticalBarPlot.create("Visits per Student", nameCounts, "Name", "Count"));

        // Analysis: Average duration per student
        Table avgDurationByName = bathroom.summarize("Duration", mean).by("Name");
        System.out.println("Average visit duration per student:");
        System.out.println(avgDurationByName);
        Plot.show(VerticalBarPlot.create("Avg Duration per Student", avgDurationByName, "Name", "Mean [Duration]"));

        // Analysis: Abnormal visits
        int totalAbnormal = bathroom.where(bathroom.booleanColumn("Abnormal").isTrue()).rowCount();
        int totalNormal = bathroom.rowCount() - totalAbnormal;
        System.out.println("Abnormal vs Normal Visits:");
        System.out.println("Abnormal (>15 min): " + totalAbnormal);
        System.out.println("Normal: " + totalNormal);

        Table abnormalCounts = Table.create("Abnormality")
                .addColumns(
                    StringColumn.create("Type", new String[]{"Normal", "Abnormal"}),
                    IntColumn.create("Count", new int[]{totalNormal, totalAbnormal})
                );
        Plot.show(VerticalBarPlot.create("Abnormal vs Normal Visits", abnormalCounts, "Type", "Count"));

        // Analysis: Duration Distribution
        Plot.show(Histogram.create("Distribution of Visit Durations", bathroom.numberColumn("Duration")));
    }
}
