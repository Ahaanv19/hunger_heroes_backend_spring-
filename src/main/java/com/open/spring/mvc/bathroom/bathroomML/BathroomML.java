package com.open.spring.mvc.bathroom.bathroomML;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/*
 * Loads bathroom CSV and uses machine learning models to make predictions
 */
public class BathroomML {
    public static void main(String[] args) throws Exception {
        // Step 1: Load Cleaned Data
        InputStream inputStream = BathroomML.class.getResourceAsStream("/data/bathroom_cleaned.csv"); // Corrected path
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: bathroom_cleaned.csv");
        }
        Table table = Table.read().csv(inputStream);

        // Step 2: Drop irrelevant columns
        table = table.removeColumns("Name", "Date");

        // Step 3: Convert "Abnormal" from Boolean to Nominal ("Yes"/"No")
        StringColumn abnormalStr = table.booleanColumn("Abnormal").asStringColumn();
        abnormalStr = abnormalStr.replaceAll("true", "Yes").replaceAll("false", "No");
        table.replaceColumn("Abnormal", abnormalStr);

        // Step 4: Normalize numeric columns
        normalizeColumn(table, "Duration");
        normalizeColumn(table, "Average Duration By Period");

        // Step 5: Convert to Weka Instances
        Instances data = convertTableToWeka(table);
        data.setClassIndex(data.attribute("Abnormal").index());

        // Step 6: Train Models
        J48 tree = new J48();
        tree.buildClassifier(data);

        Logistic logistic = new Logistic();
        logistic.buildClassifier(data);

        // Step 7: Print Models
        System.out.println("Decision Tree:\n" + tree);
        System.out.println("Logistic Regression:\n" + logistic);

        // Step 8: Evaluate with 10-fold Cross-Validation
        evaluateModel(tree, data, "Decision Tree");
        evaluateModel(logistic, data, "Logistic Regression");
    }

    // Normalize column to [0, 1] range
    private static void normalizeColumn(Table table, String columnName) {
        DoubleColumn col = table.doubleColumn(columnName);
        double min = col.min();
        double max = col.max();
        for (int i = 0; i < col.size(); i++) {
            double normalized = (col.getDouble(i) - min) / (max - min);
            col.set(i, normalized);
        }
    }

    // Convert Tablesaw Table to Weka Instances
    private static Instances convertTableToWeka(Table table) {
        List<Attribute> attributes = new ArrayList<>();
        for (Column<?> col : table.columns()) {
            if (col.type().equals(ColumnType.STRING)) {
                List<String> classValues = table.stringColumn(col.name()).unique().asList();
                attributes.add(new Attribute(col.name(), classValues));
            } else {
                attributes.add(new Attribute(col.name()));
            }
        }

        Instances data = new Instances("Bathroom", new ArrayList<>(attributes), table.rowCount());

        for (Row row : table) {
            double[] values = new double[table.columnCount()];
            for (int i = 0; i < table.columnCount(); i++) {
                Column<?> col = table.column(i);
                if (col.type() == ColumnType.STRING) {
                    values[i] = attributes.get(i).indexOfValue(row.getString(i));
                } else {
                    values[i] = row.getDouble(i);
                }
            }
            data.add(new DenseInstance(1.0, values));
        }

        return data;
    }

    // Evaluate model with 10-fold cross-validation
    private static void evaluateModel(Classifier model, Instances data, String modelName) throws Exception {
        weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(data);
        eval.crossValidateModel(model, data, 10, new java.util.Random(1));
        System.out.printf("%s Accuracy: %.2f%%%n", modelName, eval.pctCorrect());
    }
}
