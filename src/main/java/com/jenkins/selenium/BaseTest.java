package com.jenkins.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

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
}
