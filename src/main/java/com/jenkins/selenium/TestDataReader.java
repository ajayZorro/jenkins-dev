package com.jenkins.selenium;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestDataReader {
    private static final Logger logger = LoggerFactory.getLogger(TestDataReader.class);
    
    public static List<TestData> readTestData(String csvFilePath) {
        List<TestData> testDataList = new ArrayList<>();
        
        try (FileReader fileReader = new FileReader(csvFilePath);
             CSVReader csvReader = new CSVReader(fileReader)) {
            
            List<String[]> records = csvReader.readAll();
            
            // Skip header row
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                if (record.length >= 4) {
                    TestData testData = new TestData(
                        record[0].trim(), // searchTerm
                        record[1].trim(), // expectedResult
                        record[2].trim(), // testName
                        record[3].trim()  // browser
                    );
                    testDataList.add(testData);
                    logger.info("Loaded test data: {}", testData);
                } else {
                    logger.warn("Skipping invalid record at line {}: {}", i + 1, String.join(",", record));
                }
            }
            
            logger.info("Successfully loaded {} test data records", testDataList.size());
            
        } catch (IOException | CsvException e) {
            logger.error("Error reading CSV file: {}", csvFilePath, e);
            throw new RuntimeException("Failed to read CSV file", e);
        }
        
        return testDataList;
    }
}