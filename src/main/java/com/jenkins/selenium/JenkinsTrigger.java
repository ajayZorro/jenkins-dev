package com.jenkins.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenkinsTrigger {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsTrigger.class);
    
    public static void main(String[] args) {
        // Default values - can be overridden by system properties or command line args
        String jenkinsUrl = System.getProperty("jenkins.url", "http://localhost:8080");
        String username = System.getProperty("jenkins.username", "admin");
        String apiToken = System.getProperty("jenkins.token", "your-api-token");
        String jobName = System.getProperty("jenkins.job", "selenium-tests");
        
        // Override with command line arguments if provided
        if (args.length >= 4) {
            jenkinsUrl = args[0];
            username = args[1];
            apiToken = args[2];
            jobName = args[3];
        }
        
        logger.info("Triggering Jenkins job: {} at {}", jobName, jenkinsUrl);
        
        JenkinsAPI jenkinsAPI = new JenkinsAPI(jenkinsUrl, username, apiToken);
        
        try {
            // Trigger the job
            String result = jenkinsAPI.triggerJob(jobName);
            logger.info("Job trigger result: {}", result);
            
            if ("SUCCESS".equals(result)) {
                logger.info("Waiting for job to complete...");
                
                // Wait for job to complete (poll every 10 seconds for up to 10 minutes)
                int maxAttempts = 60; // 10 minutes
                int attempt = 0;
                
                while (attempt < maxAttempts) {
                    Thread.sleep(10000); // Wait 10 seconds
                    String status = jenkinsAPI.getJobStatus(jobName);
                    
                    if ("SUCCESS".equals(status)) {
                        logger.info("Job completed successfully!");
                        break;
                    } else if ("FAILURE".equals(status) || "ABORTED".equals(status)) {
                        logger.error("Job failed with status: {}", status);
                        break;
                    } else if ("UNKNOWN".equals(status)) {
                        logger.warn("Job status unknown, continuing to wait...");
                    } else {
                        logger.info("Job status: {}, continuing to wait...", status);
                    }
                    
                    attempt++;
                }
                
                if (attempt >= maxAttempts) {
                    logger.warn("Job did not complete within timeout period");
                }
                
                // Get console output
                String consoleOutput = jenkinsAPI.getJobConsoleOutput(jobName);
                logger.info("Job console output:\n{}", consoleOutput);
                
            } else {
                logger.error("Failed to trigger Jenkins job");
                System.exit(1);
            }
            
        } catch (Exception e) {
            logger.error("Error triggering Jenkins job", e);
            System.exit(1);
        } finally {
            jenkinsAPI.close();
        }
    }
}
