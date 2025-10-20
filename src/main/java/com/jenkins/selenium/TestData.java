package com.jenkins.selenium;

public class TestData {
    private String searchTerm;
    private String expectedResult;
    private String testName;
    private String browser;
    
    public TestData() {}
    
    public TestData(String searchTerm, String expectedResult, String testName, String browser) {
        this.searchTerm = searchTerm;
        this.expectedResult = expectedResult;
        this.testName = testName;
        this.browser = browser;
    }
    
    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }
    
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
    
    public String getExpectedResult() {
        return expectedResult;
    }
    
    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }
    
    public String getTestName() {
        return testName;
    }
    
    public void setTestName(String testName) {
        this.testName = testName;
    }
    
    public String getBrowser() {
        return browser;
    }
    
    public void setBrowser(String browser) {
        this.browser = browser;
    }
    
    @Override
    public String toString() {
        return "TestData{" +
                "searchTerm='" + searchTerm + '\'' +
                ", expectedResult='" + expectedResult + '\'' +
                ", testName='" + testName + '\'' +
                ", browser='" + browser + '\'' +
                '}';
    }
}
