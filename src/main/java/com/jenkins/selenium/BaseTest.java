package com.jenkins.selenium;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected WebDriverWait wait;
    
    protected void setupDriver(String browser) {
        if (browser == null || browser.isEmpty()) {
            browser = "chrome"; // default browser
        }
        
        switch (browser.toLowerCase()) {
            case "chrome":
                setupChromeDriver();
                break;
            case "firefox":
                setupFirefoxDriver();
                break;
            default:
                logger.warn("Unknown browser: {}. Using Chrome as default.", browser);
                setupChromeDriver();
        }
        
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        logger.info("WebDriver setup completed for browser: {}", browser);
    }
    
    private void setupChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        // Enable headless mode for remote Jenkins
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-images");
        
        driver = new ChromeDriver(options);
    }
    
    private void setupFirefoxDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        
        driver = new FirefoxDriver(options);
    }
    
    protected void teardownDriver() {
        if (driver != null) {
            driver.quit();
            logger.info("WebDriver closed successfully");
        }
    }
    
    /**
     * Capture screenshot and save to file
     * @param testName Name of the test for file naming
     * @return Path to the saved screenshot file
     */
    protected String captureScreenshot(String testName) {
        if (driver == null) {
            logger.warn("Driver is null, cannot capture screenshot");
            return null;
        }
        
        try {
            // Create screenshots directory
            String screenshotDir = System.getProperty("selenium.screenshot.dir", "build/screenshots");
            Path screenshotPath = Paths.get(screenshotDir);
            Files.createDirectories(screenshotPath);
            
            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s.png", testName, timestamp);
            Path filePath = screenshotPath.resolve(fileName);
            
            // Capture screenshot
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);
            
            // Save to file
            Files.write(filePath, screenshotBytes);
            
            logger.info("Screenshot captured: {}", filePath.toString());
            return filePath.toString();
            
        } catch (IOException e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Capture screenshot on test failure
     * @param testName Name of the test
     * @param errorMessage Error message for context
     */
    protected void captureScreenshotOnFailure(String testName, String errorMessage) {
        String screenshotPath = captureScreenshot(testName + "_FAILED");
        if (screenshotPath != null) {
            logger.error("Test failed: {}. Screenshot saved: {}", errorMessage, screenshotPath);
        }
    }
}
