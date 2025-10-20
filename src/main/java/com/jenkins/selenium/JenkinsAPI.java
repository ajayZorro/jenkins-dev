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

public class JenkinsAPI {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsAPI.class);
    
    private final String jenkinsUrl;
    private final String username;
    private final String apiToken;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public JenkinsAPI(String jenkinsUrl, String username, String apiToken) {
        this.jenkinsUrl = jenkinsUrl.endsWith("/") ? jenkinsUrl : jenkinsUrl + "/";
        this.username = username;
        this.apiToken = apiToken;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }
    
    public String triggerJob(String jobName) {
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
                return "SUCCESS";
            } else {
                logger.error("Failed to trigger Jenkins job: {}. Status code: {}", jobName, statusCode);
                return "FAILED";
            }
            
        } catch (IOException e) {
            logger.error("Error triggering Jenkins job: {}", jobName, e);
            return "ERROR";
        }
    }
    
    public String getJobStatus(String jobName) {
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
                String result = jsonNode.get("result").asText();
                logger.info("Job status for {}: {}", jobName, result);
                return result;
            } else {
                logger.error("Failed to get job status: {}. Status code: {}", jobName, statusCode);
                return "UNKNOWN";
            }
            
        } catch (IOException e) {
            logger.error("Error getting job status: {}", jobName, e);
            return "ERROR";
        }
    }
    
    public String getJobConsoleOutput(String jobName) {
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
                logger.info("Retrieved console output for job: {}", jobName);
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
    
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
}
