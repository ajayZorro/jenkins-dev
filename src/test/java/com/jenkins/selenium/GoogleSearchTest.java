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
    public void testEcommerceSearch(String searchTerm, String expectedResult, String testName) {
        logger.info("Starting test: {} with search term: {}", testName, searchTerm);
        
        try {
            // Navigate to Saucedemo (e-commerce demo site)
            driver.get("https://www.saucedemo.com");
            logger.info("Navigated to Saucedemo homepage");
            
            // Login with demo credentials
            driver.findElement(By.id("user-name")).sendKeys("standard_user");
            driver.findElement(By.id("password")).sendKeys("secret_sauce");
            driver.findElement(By.id("login-button")).click();
            logger.info("Logged in successfully");
            
            // Wait for products page to load
            Thread.sleep(2000);
            
            // Verify we're on the products page
            String currentUrl = driver.getCurrentUrl();
            logger.info("Current URL: {}", currentUrl);
            Assert.assertTrue(currentUrl.contains("inventory"), "Should be on products page");
            
            // Verify page title
            String title = driver.getTitle();
            logger.info("Page title: {}", title);
            Assert.assertTrue(title.toLowerCase().contains("swag labs"), "Page title should contain 'Swag Labs'");
            
            // Verify products are displayed
            List<WebElement> products = driver.findElements(By.cssSelector(".inventory_item"));
            logger.info("Found {} products", products.size());
            Assert.assertTrue(products.size() > 0, "Should have products displayed");
            
            // Verify specific product elements
            List<WebElement> productNames = driver.findElements(By.cssSelector(".inventory_item_name"));
            Assert.assertTrue(productNames.size() > 0, "Should have product names");
            
            List<WebElement> addToCartButtons = driver.findElements(By.cssSelector("button[class*='btn_inventory']"));
            Assert.assertTrue(addToCartButtons.size() > 0, "Should have add to cart buttons");
            
            logger.info("Test passed: {}", testName);
            
        } catch (Exception e) {
            logger.error("Test failed: {} - Error: {}", testName, e.getMessage());
            Assert.fail("Test failed: " + testName + " - " + e.getMessage());
        }
    }
    
    @Test
    public void testEcommerceWithCSVData() {
        String csvFilePath = System.getProperty("csv.file", "src/test/resources/testdata.csv");
        List<TestData> testDataList = TestDataReader.readTestData(csvFilePath);
        
        for (TestData testData : testDataList) {
            logger.info("Running test with CSV data: {}", testData);
            
            // Setup driver for this test data
            setupDriver(testData.getBrowser());
            
            try {
                // Navigate to Saucedemo
                driver.get("https://www.saucedemo.com");
                logger.info("Navigated to Saucedemo homepage");
                
                // Login with demo credentials
                driver.findElement(By.id("user-name")).sendKeys("standard_user");
                driver.findElement(By.id("password")).sendKeys("secret_sauce");
                driver.findElement(By.id("login-button")).click();
                logger.info("Logged in successfully");
                
                // Wait for products page to load
                Thread.sleep(2000);
                
                // Verify we're on the products page
                String currentUrl = driver.getCurrentUrl();
                logger.info("Current URL: {}", currentUrl);
                Assert.assertTrue(currentUrl.contains("inventory"), "Should be on products page for test: " + testData.getTestName());
                
                // Verify page title
                String title = driver.getTitle();
                logger.info("Page title: {}", title);
                Assert.assertTrue(title.toLowerCase().contains("swag labs"), "Page title should contain 'Swag Labs' for test: " + testData.getTestName());
                
                // Verify products are displayed
                List<WebElement> products = driver.findElements(By.cssSelector(".inventory_item"));
                logger.info("Found {} products", products.size());
                Assert.assertTrue(products.size() > 0, "Should have products displayed for test: " + testData.getTestName());
                
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
