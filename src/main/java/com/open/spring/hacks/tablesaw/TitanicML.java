package com.open.spring.hacks.tablesaw;

import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.core.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

public class TitanicML {

    public static void main(String[] args) throws Exception {
        // Step 1: Load and Clean Data using Tablesaw
        InputStream inputStream = TitanicML.class.getResourceAsStream("/data/titanic.csv");
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: titanic.csv");
        }
        Table titanic = Table.read().csv(inputStream);

        // Drop non-relevant columns
        titanic = titanic.removeColumns("Name", "Ticket", "Cabin");

        // Convert categorical values to numeric
        StringColumn sex = titanic.stringColumn("Sex");
        sex = sex.replaceAll("male", "1").replaceAll("female", "0");
        titanic.replaceColumn("Sex", sex);

        StringColumn embarked = titanic.stringColumn("Embarked");
        embarked = embarked.replaceAll("S", "0").replaceAll("C", "1").replaceAll("Q", "2");
        titanic.replaceColumn("Embarked", embarked);

        // Fill missing values
        titanic.doubleColumn("Age").setMissingTo(titanic.numberColumn("Age").median());
        titanic.doubleColumn("Fare").setMissingTo(titanic.numberColumn("Fare").median());

        // Convert "Survived" column to nominal
        StringColumn survived = titanic.intColumn("Survived").asStringColumn();
        survived = survived.replaceAll("0", "No").replaceAll("1", "Yes");
        titanic.replaceColumn("Survived", survived);

        // Normalize numeric columns
        normalizeColumn(titanic, "Age");
        normalizeColumn(titanic, "Fare");
        normalizeColumn(titanic, "SibSp");
        normalizeColumn(titanic, "Parch");

        // Step 2: Convert Tablesaw Table to Weka Instances
        Instances data = convertTableToWeka(titanic);

        // Set class index (target variable)
        // Find the Survived attribute index directly
        // In your main method, replace the current survivedIndex finder with this:
        int survivedIndex = -1;
        for (int i = 0; i < data.numAttributes(); i++) {
            // Match attribute name that starts with "Survived" since it appears to have extra text
            if (data.attribute(i).name().startsWith("Survived")) {
                survivedIndex = i;
                System.out.println("Found Survived attribute at index " + i + " with name: " + data.attribute(i).name());
                break;
            }
        }
        
        if (survivedIndex == -1) {
            // Debug: Print all available attribute names
            System.out.println("Available attributes:");
            Enumeration<Attribute> attrs = data.enumerateAttributes();
            while (attrs.hasMoreElements()) {
                System.out.println("  - " + attrs.nextElement().name());
            }
            throw new RuntimeException("Could not find 'Survived' attribute in the dataset");
        }
        
        data.setClassIndex(survivedIndex);

        // Step 3: Apply Machine Learning Models
        J48 tree = new J48();
        tree.buildClassifier(data);

        Logistic logistic = new Logistic();
        logistic.buildClassifier(data);

        // Step 4: Evaluate Models
        System.out.println("Decision Tree Model:\n" + tree);
        System.out.println("Logistic Regression Model:\n" + logistic);

        // Cross-validation evaluation
        evaluateModel(tree, data, "Decision Tree");
        evaluateModel(logistic, data, "Logistic Regression");
    }

   // Normalize a numeric column to be between 0 and 1
   private static void normalizeColumn(Table table, String columnName) {
        Column<?> column = table.column(columnName);
        double min = 0;
        double max = 0;
        if (column instanceof DoubleColumn) {
            DoubleColumn doubleColumn = (DoubleColumn) column;
            min = doubleColumn.min();
            max = doubleColumn.max();
            if (max > min) { // Prevent division by zero
                for (int i = 0; i < doubleColumn.size(); i++) {
                    double normalizedValue = (doubleColumn.getDouble(i) - min) / (max - min);
                    doubleColumn.set(i, normalizedValue);
                }
            }
        } else if (column instanceof IntColumn) {
            IntColumn intColumn = (IntColumn) column;
            min = intColumn.min();
            max = intColumn.max();
            if (max > min) { // Prevent division by zero
                for (int i = 0; i < intColumn.size(); i++) {
                    double normalizedValue = (intColumn.getInt(i) - min) / (max - min);
                    intColumn.set(i, (int) normalizedValue);
                }
            }
        }
    }
    
    // Convert Tablesaw Table to Weka Instances
    private static Instances convertTableToWeka(Table table) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        int index = 0;
        
        // Map to store column name -> attribute index
        java.util.HashMap<String, Integer> attributeMap = new java.util.HashMap<>();

        // Define attributes based on column types
        for (Column<?> col : table.columns()) {
            String columnName = col.name();
            
            if (col.type().equals(ColumnType.STRING)) {
                List<String> classValues = new ArrayList<>();
                StringColumn stringCol = table.stringColumn(columnName);
                
                // Add all unique values to the list of class values
                for (String value : stringCol.unique().asList()) {
                    classValues.add(value);
                }
                
                Attribute attr = new Attribute(columnName, classValues);
                attributes.add(attr);
                attributeMap.put(columnName, index++);
            } else {
                Attribute attr = new Attribute(columnName);
                attributes.add(attr);
                attributeMap.put(columnName, index++);
            }
        }

        // Create Weka dataset
        Instances data = new Instances("Titanic", attributes, table.rowCount());

        // Add data instances
        for (int rowIndex = 0; rowIndex < table.rowCount(); rowIndex++) {
            double[] values = new double[attributes.size()];
            
            for (int colIndex = 0; colIndex < table.columnCount(); colIndex++) {
                Column<?> col = table.column(colIndex);
                String colName = col.name();
                Integer attrIndex = attributeMap.get(colName);
                
                if (attrIndex == null) {
                    throw new RuntimeException("Could not find attribute index for column: " + colName);
                }
                
                if (col.type() == ColumnType.INTEGER) {
                    values[attrIndex] = table.intColumn(colIndex).get(rowIndex);
                } else if (col.type() == ColumnType.DOUBLE) {
                    values[attrIndex] = table.doubleColumn(colIndex).get(rowIndex);
                } else if (col.type() == ColumnType.STRING) {
                    String value = table.stringColumn(colIndex).get(rowIndex);
                    Attribute attr = attributes.get(attrIndex);
                    int valueIndex = attr.indexOfValue(value);
                    if (valueIndex == -1) {
                        System.err.println("Warning: Value '" + value + "' not found in attribute '" + 
                                          colName + "'. Using first value instead.");
                        valueIndex = 0;
                    }
                    values[attrIndex] = valueIndex;
                }
            }
            
            data.add(new DenseInstance(1.0, values));
        }
        
        return data;
    }

    // Evaluate model using 10-fold cross-validation
    private static void evaluateModel(Classifier model, Instances data, String modelName) throws Exception {
        weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(data);
        eval.crossValidateModel(model, data, 10, new java.util.Random(1));
        System.out.printf("%s Accuracy: %.2f%%%n", modelName, eval.pctCorrect());
        System.out.printf("%s Confusion Matrix:%n%s%n", modelName, eval.toMatrixString());
    }
}