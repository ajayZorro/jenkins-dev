package com.jenkins.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TestRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);
    
    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Please provide CSV file path as argument");
            System.exit(1);
        }
        
        String csvFilePath = args[0];
        logger.info("Starting test execution with CSV file: {}", csvFilePath);
        
        try {
            // Read test data from CSV
            List<TestData> testDataList = TestDataReader.readTestData(csvFilePath);
            logger.info("Loaded {} test cases from CSV", testDataList.size());
            
            int passedTests = 0;
            int failedTests = 0;
            
            // Execute each test
            for (TestData testData : testDataList) {
                logger.info("Executing test: {}", testData.getTestName());
                
                try {
                    executeTest(testData);
                    passedTests++;
                    logger.info("Test passed: {}", testData.getTestName());
                } catch (Exception e) {
                    failedTests++;
                    logger.error("Test failed: {} - Error: {}", testData.getTestName(), e.getMessage());
                }
            }
            
            // Print summary
            logger.info("Test execution completed. Passed: {}, Failed: {}", passedTests, failedTests);
            System.exit(failedTests > 0 ? 1 : 0);
            
        } catch (Exception e) {
            logger.error("Error during test execution", e);
            System.exit(1);
        }
    }
    
    private static void executeTest(TestData testData) throws Exception {
        BaseTest baseTest = new BaseTest();
        baseTest.setupDriver(testData.getBrowser());
        
        try {
            // Navigate to Google
            baseTest.driver.get("https://www.google.com");
            logger.info("Navigated to Google homepage");
            
            // Find search box and enter search term
            var searchBox = baseTest.driver.findElement(org.openqa.selenium.By.name("q"));
            searchBox.clear();
            searchBox.sendKeys(testData.getSearchTerm());
            logger.info("Entered search term: {}", testData.getSearchTerm());
            
            // Submit search
            searchBox.submit();
            logger.info("Submitted search");
            
            // Wait for results to load
            Thread.sleep(2000);
            
            // Verify we're on a search results page
            String currentUrl = baseTest.driver.getCurrentUrl();
            logger.info("Current URL: {}", currentUrl);
            if (!currentUrl.contains("search") && !currentUrl.contains("q=")) {
                throw new AssertionError("Should be on search results page for term: " + testData.getSearchTerm());
            }
            
            // Verify page title indicates search results
            String title = baseTest.driver.getTitle();
            logger.info("Page title: {}", title);
            if (!title.toLowerCase().contains(testData.getSearchTerm().toLowerCase()) && 
                !title.toLowerCase().contains("search")) {
                throw new AssertionError("Page title should indicate search results for term: " + testData.getSearchTerm());
            }
            
        } finally {
            baseTest.teardownDriver();
        }
    }
}
