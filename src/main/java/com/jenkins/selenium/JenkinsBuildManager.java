package com.jenkins.selenium;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced Jenkins Build Manager with comprehensive REST API integration
 */
public class JenkinsBuildManager {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsBuildManager.class);
    
    private final String jenkinsUrl;
    private final String username;
    private final String apiToken;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public JenkinsBuildManager(String jenkinsUrl, String username, String apiToken) {
        this.jenkinsUrl = jenkinsUrl.endsWith("/") ? jenkinsUrl : jenkinsUrl + "/";
        this.username = username;
        this.apiToken = apiToken;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Trigger a Jenkins job with parameters
     */
    public BuildResult triggerJobWithParameters(String jobName, Map<String, String> parameters) {
        String triggerUrl = jenkinsUrl + "job/" + jobName + "/buildWithParameters";
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            HttpPost post = new HttpPost(triggerUrl);
            post.setHeader("Authorization", "Basic " + encodedAuth);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            
            // Build parameter string
            StringBuilder paramString = new StringBuilder();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (paramString.length() > 0) {
                    paramString.append("&");
                }
                paramString.append(entry.getKey()).append("=").append(entry.getValue());
            }
            
            post.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(paramString.toString(), StandardCharsets.UTF_8));
            
            CloseableHttpResponse response = httpClient.execute(post);
            int statusCode = response.getCode();
            
            if (statusCode == 200 || statusCode == 201) {
                logger.info("Successfully triggered Jenkins job: {} with parameters: {}", jobName, parameters);
                return new BuildResult(true, "Job triggered successfully", null);
            } else {
                String errorMsg = "Failed to trigger Jenkins job: " + jobName + ". Status code: " + statusCode;
                logger.error(errorMsg);
                return new BuildResult(false, errorMsg, null);
            }
            
        } catch (IOException e) {
            String errorMsg = "Error triggering Jenkins job: " + jobName;
            logger.error(errorMsg, e);
            return new BuildResult(false, errorMsg, e);
        }
    }
    
    /**
     * Trigger a Jenkins job without parameters
     */
    public BuildResult triggerJob(String jobName) {
        String triggerUrl = jenkinsUrl + "job/" + jobName + "/build";
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            HttpPost post = new HttpPost(triggerUrl);
            post.setHeader("Authorization", "Basic " + encodedAuth);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            
            CloseableHttpResponse response = httpClient.execute(post);
            int statusCode = response.getCode();
            
            if (statusCode == 200 || statusCode == 201) {
                logger.info("Successfully triggered Jenkins job: {}", jobName);
                return new BuildResult(true, "Job triggered successfully", null);
            } else {
                String errorMsg = "Failed to trigger Jenkins job: " + jobName + ". Status code: " + statusCode;
                logger.error(errorMsg);
                return new BuildResult(false, errorMsg, null);
            }
            
        } catch (IOException e) {
            String errorMsg = "Error triggering Jenkins job: " + jobName;
            logger.error(errorMsg, e);
            return new BuildResult(false, errorMsg, e);
        }
    }
    
    /**
     * Get detailed job information
     */
    public JobInfo getJobInfo(String jobName) {
        String infoUrl = jenkinsUrl + "job/" + jobName + "/api/json";
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            HttpGet get = new HttpGet(infoUrl);
            get.setHeader("Authorization", "Basic " + encodedAuth);
            
            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getCode();
            
            if (statusCode == 200) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                JobInfo jobInfo = new JobInfo();
                jobInfo.setName(jsonNode.get("name").asText());
                jobInfo.setDescription(jsonNode.has("description") ? jsonNode.get("description").asText() : "");
                jobInfo.setUrl(jsonNode.get("url").asText());
                jobInfo.setColor(jsonNode.get("color").asText());
                jobInfo.setBuildable(jsonNode.get("buildable").asBoolean());
                
                // Get last build info
                if (jsonNode.has("lastBuild") && !jsonNode.get("lastBuild").isNull()) {
                    JsonNode lastBuild = jsonNode.get("lastBuild");
                    jobInfo.setLastBuildNumber(lastBuild.get("number").asInt());
                    jobInfo.setLastBuildUrl(lastBuild.get("url").asText());
                    jobInfo.setLastBuildResult(lastBuild.has("result") ? lastBuild.get("result").asText() : "UNKNOWN");
                    jobInfo.setLastBuildBuilding(lastBuild.has("building") ? lastBuild.get("building").asBoolean() : false);
                }
                
                logger.info("Retrieved job info for: {}", jobName);
                return jobInfo;
            } else {
                logger.error("Failed to get job info: {}. Status code: {}", jobName, statusCode);
                return null;
            }
            
        } catch (IOException e) {
            logger.error("Error getting job info: {}", jobName, e);
            return null;
        }
    }
    
    /**
     * Get build status
     */
    public BuildStatus getBuildStatus(String jobName, int buildNumber) {
        String statusUrl = jenkinsUrl + "job/" + jobName + "/" + buildNumber + "/api/json";
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            HttpGet get = new HttpGet(statusUrl);
            get.setHeader("Authorization", "Basic " + encodedAuth);
            
            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getCode();
            
            if (statusCode == 200) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                BuildStatus status = new BuildStatus();
                status.setBuildNumber(jsonNode.get("number").asInt());
                status.setResult(jsonNode.has("result") ? jsonNode.get("result").asText() : "UNKNOWN");
                status.setBuilding(jsonNode.has("building") ? jsonNode.get("building").asBoolean() : false);
                status.setDuration(jsonNode.has("duration") ? jsonNode.get("duration").asLong() : 0);
                status.setTimestamp(jsonNode.has("timestamp") ? jsonNode.get("timestamp").asLong() : 0);
                status.setUrl(jsonNode.get("url").asText());
                
                logger.info("Retrieved build status for job: {}, build: {}", jobName, buildNumber);
                return status;
            } else {
                logger.error("Failed to get build status: {} #{}. Status code: {}", jobName, buildNumber, statusCode);
                return null;
            }
            
        } catch (IOException e) {
            logger.error("Error getting build status: {} #{}", jobName, buildNumber, e);
            return null;
        }
    }
    
    /**
     * Get last build status
     */
    public BuildStatus getLastBuildStatus(String jobName) {
        String statusUrl = jenkinsUrl + "job/" + jobName + "/lastBuild/api/json";
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            HttpGet get = new HttpGet(statusUrl);
            get.setHeader("Authorization", "Basic " + encodedAuth);
            
            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getCode();
            
            if (statusCode == 200) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                BuildStatus status = new BuildStatus();
                status.setBuildNumber(jsonNode.get("number").asInt());
                status.setResult(jsonNode.has("result") ? jsonNode.get("result").asText() : "UNKNOWN");
                status.setBuilding(jsonNode.has("building") ? jsonNode.get("building").asBoolean() : false);
                status.setDuration(jsonNode.has("duration") ? jsonNode.get("duration").asLong() : 0);
                status.setTimestamp(jsonNode.has("timestamp") ? jsonNode.get("timestamp").asLong() : 0);
                status.setUrl(jsonNode.get("url").asText());
                
                logger.info("Retrieved last build status for job: {}", jobName);
                return status;
            } else {
                logger.error("Failed to get last build status: {}. Status code: {}", jobName, statusCode);
                return null;
            }
            
        } catch (IOException e) {
            logger.error("Error getting last build status: {}", jobName, e);
            return null;
        }
    }
    
    /**
     * Get console output for a specific build
     */
    public String getConsoleOutput(String jobName, int buildNumber) {
        String consoleUrl = jenkinsUrl + "job/" + jobName + "/" + buildNumber + "/consoleText";
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            HttpGet get = new HttpGet(consoleUrl);
            get.setHeader("Authorization", "Basic " + encodedAuth);
            
            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getCode();
            
            if (statusCode == 200) {
                String consoleOutput = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                logger.info("Retrieved console output for job: {}, build: {}", jobName, buildNumber);
                return consoleOutput;
            } else {
                logger.error("Failed to get console output: {} #{}. Status code: {}", jobName, buildNumber, statusCode);
                return "Failed to retrieve console output";
            }
            
        } catch (IOException e) {
            logger.error("Error getting console output: {} #{}", jobName, buildNumber, e);
            return "Error retrieving console output";
        }
    }
    
    /**
     * Get console output for the last build
     */
    public String getLastBuildConsoleOutput(String jobName) {
        String consoleUrl = jenkinsUrl + "job/" + jobName + "/lastBuild/consoleText";
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            HttpGet get = new HttpGet(consoleUrl);
            get.setHeader("Authorization", "Basic " + encodedAuth);
            
            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getCode();
            
            if (statusCode == 200) {
                String consoleOutput = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                logger.info("Retrieved console output for last build of job: {}", jobName);
                return consoleOutput;
            } else {
                logger.error("Failed to get console output: {}. Status code: {}", jobName, statusCode);
                return "Failed to retrieve console output";
            }
            
        } catch (IOException e) {
            logger.error("Error getting console output: {}", jobName, e);
            return "Error retrieving console output";
        }
    }
    
    /**
     * Wait for job completion with timeout
     */
    public BuildResult waitForJobCompletion(String jobName, int timeoutMinutes) {
        logger.info("Waiting for job completion: {} (timeout: {} minutes)", jobName, timeoutMinutes);
        
        long timeoutMillis = TimeUnit.MINUTES.toMillis(timeoutMinutes);
        long startTime = System.currentTimeMillis();
        long pollInterval = 10000; // 10 seconds
        
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            BuildStatus status = getLastBuildStatus(jobName);
            
            if (status == null) {
                return new BuildResult(false, "Failed to get job status", null);
            }
            
            if (!status.isBuilding()) {
                if ("SUCCESS".equals(status.getResult())) {
                    logger.info("Job completed successfully!");
                    return new BuildResult(true, "Job completed successfully", status);
                } else if ("FAILURE".equals(status.getResult()) || "ABORTED".equals(status.getResult())) {
                    logger.error("Job failed with status: {}", status.getResult());
                    return new BuildResult(false, "Job failed with status: " + status.getResult(), status);
                } else {
                    logger.warn("Job completed with unexpected status: {}", status.getResult());
                    return new BuildResult(false, "Job completed with status: " + status.getResult(), status);
                }
            }
            
            logger.info("Job is still building... ({}s elapsed)", 
                (System.currentTimeMillis() - startTime) / 1000);
            
            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new BuildResult(false, "Wait interrupted", null);
            }
        }
        
        logger.warn("Job did not complete within timeout period");
        return new BuildResult(false, "Job did not complete within timeout period", null);
    }
    
    /**
     * List all available jobs
     */
    public JobList listJobs() {
        String jobsUrl = jenkinsUrl + "api/json?tree=jobs[name,url,color,description,buildable,lastBuild[number,result,building]]";
        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        try {
            HttpGet get = new HttpGet(jobsUrl);
            get.setHeader("Authorization", "Basic " + encodedAuth);
            
            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getCode();
            
            if (statusCode == 200) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                JobList jobList = new JobList();
                for (JsonNode jobNode : jsonNode.get("jobs")) {
                    JobInfo jobInfo = new JobInfo();
                    jobInfo.setName(jobNode.get("name").asText());
                    jobInfo.setUrl(jobNode.get("url").asText());
                    jobInfo.setColor(jobNode.get("color").asText());
                    jobInfo.setDescription(jobNode.has("description") ? jobNode.get("description").asText() : "");
                    jobInfo.setBuildable(jobNode.get("buildable").asBoolean());
                    
                    if (jobNode.has("lastBuild") && !jobNode.get("lastBuild").isNull()) {
                        JsonNode lastBuild = jobNode.get("lastBuild");
                        jobInfo.setLastBuildNumber(lastBuild.get("number").asInt());
                        jobInfo.setLastBuildResult(lastBuild.has("result") ? lastBuild.get("result").asText() : "UNKNOWN");
                        jobInfo.setLastBuildBuilding(lastBuild.has("building") ? lastBuild.get("building").asBoolean() : false);
                    }
                    
                    jobList.addJob(jobInfo);
                }
                
                logger.info("Retrieved {} jobs", jobList.getJobs().size());
                return jobList;
            } else {
                logger.error("Failed to list jobs. Status code: {}", statusCode);
                return null;
            }
            
        } catch (IOException e) {
            logger.error("Error listing jobs", e);
            return null;
        }
    }
    
    /**
     * Close the HTTP client
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
    
    // Inner classes for data structures
    
    public static class BuildResult {
        private final boolean success;
        private final String message;
        private final Object data;
        
        public BuildResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
    
    public static class JobInfo {
        private String name;
        private String description;
        private String url;
        private String color;
        private boolean buildable;
        private int lastBuildNumber;
        private String lastBuildUrl;
        private String lastBuildResult;
        private boolean lastBuildBuilding;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public boolean isBuildable() { return buildable; }
        public void setBuildable(boolean buildable) { this.buildable = buildable; }
        
        public int getLastBuildNumber() { return lastBuildNumber; }
        public void setLastBuildNumber(int lastBuildNumber) { this.lastBuildNumber = lastBuildNumber; }
        
        public String getLastBuildUrl() { return lastBuildUrl; }
        public void setLastBuildUrl(String lastBuildUrl) { this.lastBuildUrl = lastBuildUrl; }
        
        public String getLastBuildResult() { return lastBuildResult; }
        public void setLastBuildResult(String lastBuildResult) { this.lastBuildResult = lastBuildResult; }
        
        public boolean isLastBuildBuilding() { return lastBuildBuilding; }
        public void setLastBuildBuilding(boolean lastBuildBuilding) { this.lastBuildBuilding = lastBuildBuilding; }
    }
    
    public static class BuildStatus {
        private int buildNumber;
        private String result;
        private boolean building;
        private long duration;
        private long timestamp;
        private String url;
        
        // Getters and setters
        public int getBuildNumber() { return buildNumber; }
        public void setBuildNumber(int buildNumber) { this.buildNumber = buildNumber; }
        
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        
        public boolean isBuilding() { return building; }
        public void setBuilding(boolean building) { this.building = building; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
    
    public static class JobList {
        private final java.util.List<JobInfo> jobs = new java.util.ArrayList<>();
        
        public void addJob(JobInfo job) { jobs.add(job); }
        public java.util.List<JobInfo> getJobs() { return jobs; }
        public int size() { return jobs.size(); }
    }
}
