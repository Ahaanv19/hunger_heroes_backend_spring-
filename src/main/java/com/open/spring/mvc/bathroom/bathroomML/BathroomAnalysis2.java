package com.open.spring.mvc.bathroom.bathroomML;

import java.io.InputStream;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.classifiers.Evaluation;
import java.io.File;
import java.util.Random;

import static tech.tablesaw.aggregate.AggregateFunctions.mean;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.Histogram;
import tech.tablesaw.plotly.api.VerticalBarPlot;

public class BathroomAnalysis2 {
    public static void main(String[] args) throws Exception {
        InputStream inputStream = BathroomAnalysis.class.getResourceAsStream("bathroom_cleaned.csv");

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

        // Machine Learning: Predicting abnormal visits using J48 Decision Tree
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("bathroom_cleaned.csv"));
        Instances data = loader.getDataSet();
        data.setClassIndex(data.numAttributes() - 1); // Assuming "Abnormal" is the last column

        J48 tree = new J48(); // Decision tree model
        tree.buildClassifier(data);
        
        // Evaluate model using cross-validation
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(tree, data, 10, new Random(1));
        System.out.println("Model Evaluation:");
        System.out.println(eval.toSummaryString());
        
        // Predict abnormality for a new instance (example)
        Instance testInstance = data.firstInstance(); // Example test case
        double prediction = tree.classifyInstance(testInstance);
        System.out.println("Predicted Abnormality: " + data.classAttribute().value((int) prediction));
    }
}
