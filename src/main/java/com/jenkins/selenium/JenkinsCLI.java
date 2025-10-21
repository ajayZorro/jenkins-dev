package com.jenkins.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Command Line Interface for Jenkins Build Management
 */
public class JenkinsCLI {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsCLI.class);
    
    private final JenkinsBuildManager buildManager;
    private final Scanner scanner;
    private final String jenkinsUrl;
    
    public JenkinsCLI(String jenkinsUrl, String username, String apiToken) {
        this.jenkinsUrl = jenkinsUrl;
        this.buildManager = new JenkinsBuildManager(jenkinsUrl, username, apiToken);
        this.scanner = new Scanner(System.in);
    }
    
    public static void main(String[] args) {
        // Parse command line arguments
        String jenkinsUrl = System.getProperty("jenkins.url", "http://localhost:8080");
        String username = System.getProperty("jenkins.username", "admin");
        String apiToken = System.getProperty("jenkins.token", "");
        
        // Override with command line arguments if provided
        if (args.length >= 3) {
            jenkinsUrl = args[0];
            username = args[1];
            apiToken = args[2];
        }
        
        if (apiToken.isEmpty()) {
            System.err.println("Error: Jenkins API token is required.");
            System.err.println("Usage: java -jar jenkins-cli.jar <jenkins-url> <username> <api-token>");
            System.err.println("Or set system properties: -Djenkins.url=... -Djenkins.username=... -Djenkins.token=...");
            System.exit(1);
        }
        
        JenkinsCLI cli = new JenkinsCLI(jenkinsUrl, username, apiToken);
        cli.run();
    }
    
    public void run() {
        logger.info("Jenkins CLI started");
        logger.info("Connected to Jenkins at: {}", jenkinsUrl);
        
        while (true) {
            showMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice.toLowerCase()) {
                case "1":
                    listJobs();
                    break;
                case "2":
                    triggerJob();
                    break;
                case "3":
                    triggerJobWithParameters();
                    break;
                case "4":
                    getJobStatus();
                    break;
                case "5":
                    getConsoleOutput();
                    break;
                case "6":
                    waitForJobCompletion();
                    break;
                case "7":
                    getJobInfo();
                    break;
                case "h":
                case "help":
                    showHelp();
                    break;
                case "q":
                case "quit":
                case "exit":
                    logger.info("Exiting Jenkins CLI");
                    buildManager.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    private void showMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           JENKINS BUILD MANAGER CLI");
        System.out.println("=".repeat(50));
        System.out.println("1. List all jobs");
        System.out.println("2. Trigger job (without parameters)");
        System.out.println("3. Trigger job with parameters");
        System.out.println("4. Get job status");
        System.out.println("5. Get console output");
        System.out.println("6. Wait for job completion");
        System.out.println("7. Get job information");
        System.out.println("h. Show help");
        System.out.println("q. Quit");
        System.out.println("=".repeat(50));
        System.out.print("Enter your choice: ");
    }
    
    private void showHelp() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                           HELP");
        System.out.println("=".repeat(60));
        System.out.println("This CLI provides the following functionality:");
        System.out.println();
        System.out.println("1. List all jobs - Shows all available Jenkins jobs");
        System.out.println("2. Trigger job - Triggers a job without parameters");
        System.out.println("3. Trigger job with parameters - Triggers a job with custom parameters");
        System.out.println("4. Get job status - Shows the status of a specific job");
        System.out.println("5. Get console output - Shows the console output of a job");
        System.out.println("6. Wait for job completion - Waits for a job to complete");
        System.out.println("7. Get job information - Shows detailed job information");
        System.out.println();
        System.out.println("Common parameters for Selenium tests:");
        System.out.println("- BROWSER: chrome, firefox");
        System.out.println("- CSV_FILE: Path to test data file");
        System.out.println("=".repeat(60));
    }
    
    private void listJobs() {
        System.out.println("\nFetching jobs...");
        JenkinsBuildManager.JobList jobList = buildManager.listJobs();
        
        if (jobList == null) {
            System.out.println("Failed to retrieve jobs list.");
            return;
        }
        
        System.out.println("\nAvailable Jobs:");
        System.out.println("-".repeat(80));
        System.out.printf("%-30s %-15s %-10s %-15s%n", "Job Name", "Status", "Buildable", "Last Build");
        System.out.println("-".repeat(80));
        
        for (JenkinsBuildManager.JobInfo job : jobList.getJobs()) {
            String status = job.getLastBuildResult() != null ? job.getLastBuildResult() : "N/A";
            String buildable = job.isBuildable() ? "Yes" : "No";
            String lastBuild = job.getLastBuildNumber() > 0 ? 
                "#" + job.getLastBuildNumber() : "N/A";
            
            System.out.printf("%-30s %-15s %-10s %-15s%n", 
                job.getName(), status, buildable, lastBuild);
        }
        
        System.out.println("-".repeat(80));
        System.out.println("Total jobs: " + jobList.size());
    }
    
    private void triggerJob() {
        System.out.print("\nEnter job name to trigger: ");
        String jobName = scanner.nextLine().trim();
        
        if (jobName.isEmpty()) {
            System.out.println("Job name cannot be empty.");
            return;
        }
        
        System.out.println("Triggering job: " + jobName);
        JenkinsBuildManager.BuildResult result = buildManager.triggerJob(jobName);
        
        if (result.isSuccess()) {
            System.out.println("✓ " + result.getMessage());
        } else {
            System.out.println("✗ " + result.getMessage());
        }
    }
    
    private void triggerJobWithParameters() {
        System.out.print("\nEnter job name to trigger: ");
        String jobName = scanner.nextLine().trim();
        
        if (jobName.isEmpty()) {
            System.out.println("Job name cannot be empty.");
            return;
        }
        
        Map<String, String> parameters = new HashMap<>();
        
        // Get browser parameter
        System.out.print("Enter browser (chrome/firefox) [chrome]: ");
        String browser = scanner.nextLine().trim();
        if (browser.isEmpty()) browser = "chrome";
        parameters.put("BROWSER", browser);
        
        // Get CSV file parameter
        System.out.print("Enter CSV file path [src/test/resources/testdata.csv]: ");
        String csvFile = scanner.nextLine().trim();
        if (csvFile.isEmpty()) csvFile = "src/test/resources/testdata.csv";
        parameters.put("CSV_FILE", csvFile);
        
        // Ask for additional parameters
        System.out.println("Enter additional parameters (press Enter when done):");
        while (true) {
            System.out.print("Parameter name (or Enter to finish): ");
            String paramName = scanner.nextLine().trim();
            if (paramName.isEmpty()) break;
            
            System.out.print("Parameter value: ");
            String paramValue = scanner.nextLine().trim();
            if (!paramValue.isEmpty()) {
                parameters.put(paramName, paramValue);
            }
        }
        
        System.out.println("\nTriggering job: " + jobName + " with parameters: " + parameters);
        JenkinsBuildManager.BuildResult result = buildManager.triggerJobWithParameters(jobName, parameters);
        
        if (result.isSuccess()) {
            System.out.println("✓ " + result.getMessage());
        } else {
            System.out.println("✗ " + result.getMessage());
        }
    }
    
    private void getJobStatus() {
        System.out.print("\nEnter job name: ");
        String jobName = scanner.nextLine().trim();
        
        if (jobName.isEmpty()) {
            System.out.println("Job name cannot be empty.");
            return;
        }
        
        System.out.println("Getting status for job: " + jobName);
        JenkinsBuildManager.BuildStatus status = buildManager.getLastBuildStatus(jobName);
        
        if (status == null) {
            System.out.println("Failed to get job status.");
            return;
        }
        
        System.out.println("\nJob Status:");
        System.out.println("-".repeat(40));
        System.out.println("Build Number: #" + status.getBuildNumber());
        System.out.println("Result: " + status.getResult());
        System.out.println("Building: " + (status.isBuilding() ? "Yes" : "No"));
        System.out.println("Duration: " + formatDuration(status.getDuration()));
        System.out.println("URL: " + status.getUrl());
        System.out.println("-".repeat(40));
    }
    
    private void getConsoleOutput() {
        System.out.print("\nEnter job name: ");
        String jobName = scanner.nextLine().trim();
        
        if (jobName.isEmpty()) {
            System.out.println("Job name cannot be empty.");
            return;
        }
        
        System.out.print("Enter build number (or Enter for last build): ");
        String buildNumberStr = scanner.nextLine().trim();
        
        String consoleOutput;
        if (buildNumberStr.isEmpty()) {
            consoleOutput = buildManager.getLastBuildConsoleOutput(jobName);
        } else {
            try {
                int buildNumber = Integer.parseInt(buildNumberStr);
                consoleOutput = buildManager.getConsoleOutput(jobName, buildNumber);
            } catch (NumberFormatException e) {
                System.out.println("Invalid build number.");
                return;
            }
        }
        
        System.out.println("\nConsole Output:");
        System.out.println("=".repeat(80));
        System.out.println(consoleOutput);
        System.out.println("=".repeat(80));
    }
    
    private void waitForJobCompletion() {
        System.out.print("\nEnter job name: ");
        String jobName = scanner.nextLine().trim();
        
        if (jobName.isEmpty()) {
            System.out.println("Job name cannot be empty.");
            return;
        }
        
        System.out.print("Enter timeout in minutes [30]: ");
        String timeoutStr = scanner.nextLine().trim();
        int timeout = 30;
        if (!timeoutStr.isEmpty()) {
            try {
                timeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid timeout, using default: 30 minutes");
            }
        }
        
        System.out.println("Waiting for job completion: " + jobName + " (timeout: " + timeout + " minutes)");
        JenkinsBuildManager.BuildResult result = buildManager.waitForJobCompletion(jobName, timeout);
        
        if (result.isSuccess()) {
            System.out.println("✓ " + result.getMessage());
        } else {
            System.out.println("✗ " + result.getMessage());
        }
    }
    
    private void getJobInfo() {
        System.out.print("\nEnter job name: ");
        String jobName = scanner.nextLine().trim();
        
        if (jobName.isEmpty()) {
            System.out.println("Job name cannot be empty.");
            return;
        }
        
        System.out.println("Getting information for job: " + jobName);
        JenkinsBuildManager.JobInfo jobInfo = buildManager.getJobInfo(jobName);
        
        if (jobInfo == null) {
            System.out.println("Failed to get job information.");
            return;
        }
        
        System.out.println("\nJob Information:");
        System.out.println("-".repeat(50));
        System.out.println("Name: " + jobInfo.getName());
        System.out.println("Description: " + jobInfo.getDescription());
        System.out.println("URL: " + jobInfo.getUrl());
        System.out.println("Color: " + jobInfo.getColor());
        System.out.println("Buildable: " + (jobInfo.isBuildable() ? "Yes" : "No"));
        System.out.println("Last Build Number: " + jobInfo.getLastBuildNumber());
        System.out.println("Last Build Result: " + jobInfo.getLastBuildResult());
        System.out.println("Last Build Building: " + (jobInfo.isLastBuildBuilding() ? "Yes" : "No"));
        System.out.println("-".repeat(50));
    }
    
    private String formatDuration(long durationMs) {
        if (durationMs == 0) return "N/A";
        
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
