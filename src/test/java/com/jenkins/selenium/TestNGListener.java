package com.jenkins.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener for enhanced test reporting
 */
public class TestNGListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(TestNGListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Starting test: {} in class: {}", 
            result.getMethod().getMethodName(), 
            result.getTestClass().getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test PASSED: {} in class: {} (Duration: {}ms)", 
            result.getMethod().getMethodName(),
            result.getTestClass().getName(),
            result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test FAILED: {} in class: {} (Duration: {}ms)", 
            result.getMethod().getMethodName(),
            result.getTestClass().getName(),
            result.getEndMillis() - result.getStartMillis());
        
        // Log the exception details
        if (result.getThrowable() != null) {
            logger.error("Failure reason: {}", result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test SKIPPED: {} in class: {}", 
            result.getMethod().getMethodName(),
            result.getTestClass().getName());
    }
}
