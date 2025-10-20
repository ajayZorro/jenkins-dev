package com.jenkins.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("Selenium Test Automation Application");
        logger.info("Available commands:");
        logger.info("1. Run tests with CSV data: java -cp <classpath> com.jenkins.selenium.TestRunner <csv-file-path>");
        logger.info("2. Trigger Jenkins job: java -cp <classpath> com.jenkins.selenium.JenkinsTrigger [jenkins-url] [username] [api-token] [job-name]");
        logger.info("3. Run TestNG tests: gradle test");
        logger.info("4. Run tests with CSV: gradle runTestsWithCSV");
        logger.info("5. Trigger Jenkins: gradle triggerJenkinsJob");
        
        if (args.length > 0) {
            String command = args[0];
            switch (command.toLowerCase()) {
                case "test":
                    if (args.length > 1) {
                        TestRunner.main(new String[]{args[1]});
                    } else {
                        logger.error("Please provide CSV file path for test command");
                    }
                    break;
                case "jenkins":
                    String[] jenkinsArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, jenkinsArgs, 0, jenkinsArgs.length);
                    JenkinsTrigger.main(jenkinsArgs);
                    break;
                default:
                    logger.error("Unknown command: {}. Use 'test' or 'jenkins'", command);
            }
        }
    }
}
