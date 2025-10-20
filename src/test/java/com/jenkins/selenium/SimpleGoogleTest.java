package com.jenkins.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleGoogleTest extends BaseTest {
    
    @BeforeMethod
    public void setUp() {
        setupDriver(System.getProperty("browser", "chrome"));
    }
    
    @AfterMethod
    public void tearDown() {
        teardownDriver();
    }
    
    @Test
    public void testGooglePageLoads() {
        logger.info("Testing Google page loads");
        
        try {
            // Navigate to Google
            driver.get("https://www.google.com");
            logger.info("Navigated to Google homepage");
            
            // Verify page title contains Google
            String title = driver.getTitle();
            logger.info("Page title: {}", title);
            Assert.assertTrue(title.toLowerCase().contains("google"), "Page title should contain 'Google'");
            
            // Verify search box is present
            WebElement searchBox = driver.findElement(By.name("q"));
            Assert.assertNotNull(searchBox, "Search box should be present");
            
            logger.info("Test passed: Google page loads correctly");
            
        } catch (Exception e) {
            logger.error("Test failed: {}", e.getMessage());
            Assert.fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testGoogleSearchWorks() {
        logger.info("Testing Google search functionality");
        
        try {
            // Navigate to Google
            driver.get("https://www.google.com");
            logger.info("Navigated to Google homepage");
            
            // Find search box and enter search term
            WebElement searchBox = driver.findElement(By.name("q"));
            searchBox.clear();
            searchBox.sendKeys("Jenkins");
            logger.info("Entered search term: Jenkins");
            
            // Submit search
            searchBox.submit();
            logger.info("Submitted search");
            
            // Wait for results to load
            Thread.sleep(3000);
            
            // Verify we're on a search results page
            String currentUrl = driver.getCurrentUrl();
            logger.info("Current URL: {}", currentUrl);
            Assert.assertTrue(currentUrl.contains("search") || currentUrl.contains("q="), 
                "Should be on search results page");
            
            // Verify page title indicates search results
            String title = driver.getTitle();
            logger.info("Page title: {}", title);
            Assert.assertTrue(title.toLowerCase().contains("jenkins") || title.toLowerCase().contains("search"), 
                "Page title should indicate search results");
            
            logger.info("Test passed: Google search works correctly");
            
        } catch (Exception e) {
            logger.error("Test failed: {}", e.getMessage());
            Assert.fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testGoogleSearchWithCSVData() {
        logger.info("Testing Google search with CSV data");
        
        String csvFilePath = System.getProperty("csv.file", "src/test/resources/testdata.csv");
        var testDataList = TestDataReader.readTestData(csvFilePath);
        
        for (var testData : testDataList) {
            logger.info("Running test with CSV data: {}", testData);
            
            // Setup driver for this test data
            setupDriver(testData.getBrowser());
            
            try {
                // Navigate to Google
                driver.get("https://www.google.com");
                logger.info("Navigated to Google homepage");
                
                // Find search box and enter search term
                WebElement searchBox = driver.findElement(By.name("q"));
                searchBox.clear();
                searchBox.sendKeys(testData.getSearchTerm());
                logger.info("Entered search term: {}", testData.getSearchTerm());
                
                // Submit search
                searchBox.submit();
                logger.info("Submitted search");
                
                // Wait for results to load
                Thread.sleep(3000);
                
                // Verify we're on a search results page
                String currentUrl = driver.getCurrentUrl();
                logger.info("Current URL: {}", currentUrl);
                Assert.assertTrue(currentUrl.contains("search") || currentUrl.contains("q="), 
                    "Should be on search results page for term: " + testData.getSearchTerm());
                
                logger.info("Test passed: {}", testData.getTestName());
                
            } catch (Exception e) {
                logger.error("Test failed: {} - Error: {}", testData.getTestName(), e.getMessage());
                Assert.fail("Test failed: " + testData.getTestName() + " - " + e.getMessage());
            } finally {
                teardownDriver();
            }
        }
    }
}
