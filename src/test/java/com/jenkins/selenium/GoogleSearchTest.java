package com.jenkins.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class GoogleSearchTest extends BaseTest {
    
    @BeforeMethod
    public void setUp() {
        setupDriver(System.getProperty("browser", "chrome"));
    }
    
    @AfterMethod
    public void tearDown() {
        teardownDriver();
    }
    
    @DataProvider(name = "searchData")
    public Object[][] getSearchData() {
        return new Object[][] {
            {"Jenkins", "Jenkins", "Test Jenkins Search"},
            {"Selenium", "Selenium", "Test Selenium Search"},
            {"Java", "Java", "Test Java Search"},
            {"Gradle", "Gradle", "Test Gradle Search"},
            {"TestNG", "TestNG", "Test TestNG Search"}
        };
    }
    
    @Test(dataProvider = "searchData")
    public void testGoogleSearch(String searchTerm, String expectedResult, String testName) {
        logger.info("Starting test: {} with search term: {}", testName, searchTerm);
        
        try {
            // Navigate to Google
            driver.get("https://www.google.com");
            logger.info("Navigated to Google homepage");
            
            // Find search box and enter search term
            WebElement searchBox = driver.findElement(By.name("q"));
            searchBox.clear();
            searchBox.sendKeys(searchTerm);
            logger.info("Entered search term: {}", searchTerm);
            
            // Submit search
            searchBox.submit();
            logger.info("Submitted search");
            
            // Wait for results to load
            Thread.sleep(3000);
            
            // Log current page title and URL for debugging
            logger.info("Current page title: {}", driver.getTitle());
            logger.info("Current page URL: {}", driver.getCurrentUrl());
            
            // Try multiple selectors for search results
            List<WebElement> searchResults = driver.findElements(By.cssSelector("h3"));
            if (searchResults.isEmpty()) {
                // Try alternative selectors
                searchResults = driver.findElements(By.cssSelector(".g h3"));
                if (searchResults.isEmpty()) {
                    searchResults = driver.findElements(By.cssSelector("[data-ved] h3"));
                }
            }
            
            logger.info("Found {} search result elements", searchResults.size());
            
            boolean found = false;
            for (WebElement result : searchResults) {
                String resultText = result.getText();
                logger.info("Search result text: '{}'", resultText);
                if (resultText.toLowerCase().contains(expectedResult.toLowerCase())) {
                    found = true;
                    logger.info("Found expected result: {}", resultText);
                    break;
                }
            }
            
            // If not found in h3 elements, try other elements
            if (!found) {
                List<WebElement> allResults = driver.findElements(By.cssSelector(".g"));
                logger.info("Checking {} result containers", allResults.size());
                for (WebElement container : allResults) {
                    String containerText = container.getText();
                    if (containerText.toLowerCase().contains(expectedResult.toLowerCase())) {
                        found = true;
                        logger.info("Found expected result in container: {}", containerText.substring(0, Math.min(100, containerText.length())));
                        break;
                    }
                }
            }
            
            Assert.assertTrue(found, "Expected result '" + expectedResult + "' not found in search results for term: " + searchTerm);
            logger.info("Test passed: {}", testName);
            
        } catch (Exception e) {
            logger.error("Test failed: {} - Error: {}", testName, e.getMessage());
            Assert.fail("Test failed: " + testName + " - " + e.getMessage());
        }
    }
    
    @Test
    public void testGoogleSearchWithCSVData() {
        String csvFilePath = System.getProperty("csv.file", "src/test/resources/testdata.csv");
        List<TestData> testDataList = TestDataReader.readTestData(csvFilePath);
        
        for (TestData testData : testDataList) {
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
                Thread.sleep(2000);
                
                // Verify search results contain expected result
                List<WebElement> searchResults = driver.findElements(By.cssSelector("h3"));
                boolean found = false;
                
                for (WebElement result : searchResults) {
                    if (result.getText().toLowerCase().contains(testData.getExpectedResult().toLowerCase())) {
                        found = true;
                        logger.info("Found expected result: {}", result.getText());
                        break;
                    }
                }
                
                Assert.assertTrue(found, "Expected result '" + testData.getExpectedResult() + 
                    "' not found in search results for term: " + testData.getSearchTerm());
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
