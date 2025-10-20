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
            // Navigate to Saucedemo (e-commerce demo site)
            baseTest.driver.get("https://www.saucedemo.com");
            logger.info("Navigated to Saucedemo homepage");
            
            // Login with demo credentials
            baseTest.driver.findElement(org.openqa.selenium.By.id("user-name")).sendKeys("standard_user");
            baseTest.driver.findElement(org.openqa.selenium.By.id("password")).sendKeys("secret_sauce");
            baseTest.driver.findElement(org.openqa.selenium.By.id("login-button")).click();
            logger.info("Logged in successfully");
            
            // Wait for products page to load
            Thread.sleep(2000);
            
            // Verify we're on the products page
            String currentUrl = baseTest.driver.getCurrentUrl();
            logger.info("Current URL: {}", currentUrl);
            if (!currentUrl.contains("inventory")) {
                throw new AssertionError("Should be on products page for test: " + testData.getTestName());
            }
            
            // Verify page title
            String title = baseTest.driver.getTitle();
            logger.info("Page title: {}", title);
            if (!title.toLowerCase().contains("swag labs")) {
                throw new AssertionError("Page title should contain 'Swag Labs' for test: " + testData.getTestName());
            }
            
            // Verify products are displayed
            var products = baseTest.driver.findElements(org.openqa.selenium.By.cssSelector(".inventory_item"));
            logger.info("Found {} products", products.size());
            if (products.size() == 0) {
                throw new AssertionError("Should have products displayed for test: " + testData.getTestName());
            }
            
        } finally {
            baseTest.teardownDriver();
        }
    }
}
