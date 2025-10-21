package com.jenkins.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Test Runner that integrates with Jenkins for automated testing
 */
public class JenkinsTestRunner {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsTestRunner.class);
    
    private final JenkinsBuildManager buildManager;
    private final String defaultJobName;
    
    public JenkinsTestRunner(String jenkinsUrl, String username, String apiToken, String defaultJobName) {
        this.buildManager = new JenkinsBuildManager(jenkinsUrl, username, apiToken);
        this.defaultJobName = defaultJobName;
    }
    
    public static void main(String[] args) {
        // Parse command line arguments
        String jenkinsUrl = System.getProperty("jenkins.url", "http://localhost:8080");
        String username = System.getProperty("jenkins.username", "admin");
        String apiToken = System.getProperty("jenkins.token", "");
        String jobName = System.getProperty("jenkins.job", "selenium-tests");
        String browser = System.getProperty("browser", "chrome");
        String csvFile = System.getProperty("csv.file", "src/test/resources/testdata.csv");
        boolean waitForCompletion = Boolean.parseBoolean(System.getProperty("wait", "true"));
        int timeoutMinutes = Integer.parseInt(System.getProperty("timeout", "30"));
        
        // Override with command line arguments if provided
        if (args.length >= 3) {
            jenkinsUrl = args[0];
            username = args[1];
            apiToken = args[2];
            if (args.length >= 4) jobName = args[3];
            if (args.length >= 5) browser = args[4];
            if (args.length >= 6) csvFile = args[5];
        }
        
        if (apiToken.isEmpty()) {
            System.err.println("Error: Jenkins API token is required.");
            System.err.println("Usage: java -jar jenkins-test-runner.jar <jenkins-url> <username> <api-token> [job-name] [browser] [csv-file]");
            System.err.println("Or set system properties: -Djenkins.url=... -Djenkins.username=... -Djenkins.token=...");
            System.exit(1);
        }
        
        JenkinsTestRunner runner = new JenkinsTestRunner(jenkinsUrl, username, apiToken, jobName);
        
        try {
            TestResult result = runner.runTests(browser, csvFile, waitForCompletion, timeoutMinutes);
            System.exit(result.isSuccess() ? 0 : 1);
        } finally {
            runner.close();
        }
    }
    
    /**
     * Run tests with specified parameters
     */
    public TestResult runTests(String browser, String csvFile, boolean waitForCompletion, int timeoutMinutes) {
        logger.info("Starting test execution");
        logger.info("Job: {}, Browser: {}, CSV File: {}", defaultJobName, browser, csvFile);
        
        // Prepare parameters
        Map<String, String> parameters = new HashMap<>();
        parameters.put("BROWSER", browser);
        parameters.put("CSV_FILE", csvFile);
        
        // Trigger the job
        logger.info("Triggering Jenkins job: {}", defaultJobName);
        JenkinsBuildManager.BuildResult triggerResult = buildManager.triggerJobWithParameters(defaultJobName, parameters);
        
        if (!triggerResult.isSuccess()) {
            logger.error("Failed to trigger Jenkins job: {}", triggerResult.getMessage());
            return new TestResult(false, "Failed to trigger job: " + triggerResult.getMessage(), null);
        }
        
        logger.info("Job triggered successfully");
        
        if (waitForCompletion) {
            logger.info("Waiting for job completion (timeout: {} minutes)", timeoutMinutes);
            JenkinsBuildManager.BuildResult waitResult = buildManager.waitForJobCompletion(defaultJobName, timeoutMinutes);
            
            if (waitResult.isSuccess()) {
                logger.info("Job completed successfully");
                return new TestResult(true, "Tests completed successfully", waitResult.getData());
            } else {
                logger.error("Job failed or timed out: {}", waitResult.getMessage());
                return new TestResult(false, "Tests failed: " + waitResult.getMessage(), waitResult.getData());
            }
        } else {
            logger.info("Job triggered, not waiting for completion");
            return new TestResult(true, "Job triggered successfully", null);
        }
    }
    
    /**
     * Run tests with default parameters
     */
    public TestResult runTests() {
        return runTests("chrome", "src/test/resources/testdata.csv", true, 30);
    }
    
    /**
     * Run tests with custom browser
     */
    public TestResult runTests(String browser) {
        return runTests(browser, "src/test/resources/testdata.csv", true, 30);
    }
    
    /**
     * Run tests with custom browser and CSV file
     */
    public TestResult runTests(String browser, String csvFile) {
        return runTests(browser, csvFile, true, 30);
    }
    
    /**
     * Get the status of the last test run
     */
    public TestStatus getTestStatus() {
        JenkinsBuildManager.BuildStatus buildStatus = buildManager.getLastBuildStatus(defaultJobName);
        
        if (buildStatus == null) {
            return new TestStatus("UNKNOWN", false, "Failed to get build status", 0, 0);
        }
        
        return new TestStatus(
            buildStatus.getResult(),
            buildStatus.isBuilding(),
            buildStatus.getUrl(),
            buildStatus.getDuration(),
            buildStatus.getTimestamp()
        );
    }
    
    /**
     * Get console output of the last test run
     */
    public String getTestConsoleOutput() {
        return buildManager.getLastBuildConsoleOutput(defaultJobName);
    }
    
    /**
     * Get detailed test results
     */
    public TestResults getTestResults() {
        JenkinsBuildManager.BuildStatus buildStatus = buildManager.getLastBuildStatus(defaultJobName);
        String consoleOutput = buildManager.getLastBuildConsoleOutput(defaultJobName);
        
        return new TestResults(
            buildStatus != null ? buildStatus.getResult() : "UNKNOWN",
            buildStatus != null ? buildStatus.isBuilding() : false,
            buildStatus != null ? buildStatus.getUrl() : "",
            buildStatus != null ? buildStatus.getDuration() : 0,
            buildStatus != null ? buildStatus.getTimestamp() : 0,
            consoleOutput
        );
    }
    
    /**
     * Run multiple test scenarios
     */
    public Map<String, TestResult> runMultipleTestScenarios(Map<String, TestScenario> scenarios) {
        Map<String, TestResult> results = new HashMap<>();
        
        logger.info("Running {} test scenarios", scenarios.size());
        
        for (Map.Entry<String, TestScenario> entry : scenarios.entrySet()) {
            String scenarioName = entry.getKey();
            TestScenario scenario = entry.getValue();
            
            logger.info("Running scenario: {}", scenarioName);
            
            TestResult result = runTests(
                scenario.getBrowser(),
                scenario.getCsvFile(),
                scenario.isWaitForCompletion(),
                scenario.getTimeoutMinutes()
            );
            
            results.put(scenarioName, result);
            
            if (result.isSuccess()) {
                logger.info("Scenario '{}' completed successfully", scenarioName);
            } else {
                logger.error("Scenario '{}' failed: {}", scenarioName, result.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Close the build manager
     */
    public void close() {
        buildManager.close();
    }
    
    // Inner classes for data structures
    
    public static class TestResult {
        private final boolean success;
        private final String message;
        private final Object data;
        
        public TestResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
    
    public static class TestStatus {
        private final String result;
        private final boolean building;
        private final String url;
        private final long duration;
        private final long timestamp;
        
        public TestStatus(String result, boolean building, String url, long duration, long timestamp) {
            this.result = result;
            this.building = building;
            this.url = url;
            this.duration = duration;
            this.timestamp = timestamp;
        }
        
        public String getResult() { return result; }
        public boolean isBuilding() { return building; }
        public String getUrl() { return url; }
        public long getDuration() { return duration; }
        public long getTimestamp() { return timestamp; }
    }
    
    public static class TestResults extends TestStatus {
        private final String consoleOutput;
        
        public TestResults(String result, boolean building, String url, long duration, long timestamp, String consoleOutput) {
            super(result, building, url, duration, timestamp);
            this.consoleOutput = consoleOutput;
        }
        
        public String getConsoleOutput() { return consoleOutput; }
    }
    
    public static class TestScenario {
        private final String browser;
        private final String csvFile;
        private final boolean waitForCompletion;
        private final int timeoutMinutes;
        
        public TestScenario(String browser, String csvFile, boolean waitForCompletion, int timeoutMinutes) {
            this.browser = browser;
            this.csvFile = csvFile;
            this.waitForCompletion = waitForCompletion;
            this.timeoutMinutes = timeoutMinutes;
        }
        
        public String getBrowser() { return browser; }
        public String getCsvFile() { return csvFile; }
        public boolean isWaitForCompletion() { return waitForCompletion; }
        public int getTimeoutMinutes() { return timeoutMinutes; }
        
        public static TestScenario create(String browser, String csvFile) {
            return new TestScenario(browser, csvFile, true, 30);
        }
        
        public static TestScenario create(String browser, String csvFile, boolean waitForCompletion) {
            return new TestScenario(browser, csvFile, waitForCompletion, 30);
        }
    }
}
