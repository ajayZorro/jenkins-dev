# Selenium Test Automation with Jenkins Integration

This project provides a Java Selenium test automation framework that can be triggered from Jenkins using both scripts and AI agentic flows.

## Features

- **5 Test Cases**: Google search functionality tests with CSV data-driven approach
- **Jenkins Integration**: REST API calls to trigger Jenkins jobs
- **CSV Data Support**: Test parameters loaded from CSV files
- **Multiple Browser Support**: Chrome and Firefox
- **TestNG Framework**: For test execution and reporting
- **Gradle Build System**: Easy dependency management and task execution

## Project Structure

```
jenkins-dev/
├── src/
│   ├── main/java/com/jenkins/selenium/
│   │   ├── BaseTest.java              # Base test class with WebDriver setup
│   │   ├── TestData.java              # Test data model
│   │   ├── CSVReader.java             # CSV file reader
│   │   ├── JenkinsAPI.java            # Jenkins REST API client
│   │   ├── TestRunner.java            # Test execution runner
│   │   ├── JenkinsTrigger.java        # Jenkins job trigger
│   │   └── Main.java                  # Main application entry point
│   └── test/
│       ├── java/com/jenkins/selenium/
│       │   └── GoogleSearchTest.java  # Test cases
│       └── resources/
│           ├── testdata.csv           # Test data file
│           └── testng.xml             # TestNG configuration
├── scripts/
│   ├── run-tests.bat                  # Windows batch script
│   └── run-tests.sh                   # Linux/Mac shell script
├── build.gradle                       # Gradle build configuration
├── Jenkinsfile                        # Jenkins pipeline
└── README.md                          # This file
```

## Prerequisites

- Java 11 or higher
- Gradle 8.4 or higher
- Chrome and/or Firefox browser
- Jenkins server (for integration testing)

## Setup Instructions

### 1. Clone and Build

```bash
git clone <repository-url>
cd jenkins-dev
./gradlew build
```

### 2. Download WebDriver

The project uses WebDriverManager to automatically download browser drivers, but you can also download them manually:

- **ChromeDriver**: Download from https://chromedriver.chromium.org/
- **GeckoDriver**: Download from https://github.com/mozilla/geckodriver/releases

### 3. Configure Test Data

Edit `src/test/resources/testdata.csv` to customize your test data:

```csv
searchTerm,expectedResult,testName,browser
Jenkins,Jenkins,Test Jenkins Search,chrome
Selenium,Selenium,Test Selenium Search,chrome
Java,Java,Test Java Search,firefox
Gradle,Gradle,Test Gradle Search,chrome
TestNG,TestNG,Test TestNG Search,chrome
```

## Running Tests

### 1. Run All Tests with TestNG

```bash
./gradlew test
```

### 2. Run Tests with CSV Data

```bash
./gradlew runTestsWithCSV
```

### 3. Run Specific Test Class

```bash
./gradlew test --tests GoogleSearchTest
```

### 4. Run with Custom Browser

```bash
./gradlew test -Dbrowser=firefox
```

## Jenkins Integration

### 1. Using Scripts

#### Windows:
```cmd
scripts\run-tests.bat
```

#### Linux/Mac:
```bash
chmod +x scripts/run-tests.sh
./scripts/run-tests.sh
```

### 2. Using Gradle Tasks

#### Trigger Jenkins Job:
```bash
./gradlew triggerJenkinsJob -Djenkins.url=http://your-jenkins-url -Djenkins.username=your-username -Djenkins.token=your-token -Djenkins.job=your-job-name
```

### 3. Using Jenkins Pipeline

1. Create a new Pipeline job in Jenkins
2. Copy the contents of `Jenkinsfile` into the pipeline script
3. Configure the parameters as needed
4. Run the pipeline

### 4. REST API Integration

The project includes a `JenkinsAPI` class that provides methods to:

- `triggerJob(String jobName)`: Trigger a Jenkins job
- `getJobStatus(String jobName)`: Get job status
- `getJobConsoleOutput(String jobName)`: Get console output

Example usage:

```java
JenkinsAPI jenkinsAPI = new JenkinsAPI("http://localhost:8080", "admin", "your-token");
String result = jenkinsAPI.triggerJob("selenium-tests");
```

## Test Cases

The project includes 5 test cases:

1. **Test Jenkins Search**: Searches for "Jenkins" and verifies results
2. **Test Selenium Search**: Searches for "Selenium" and verifies results
3. **Test Java Search**: Searches for "Java" and verifies results
4. **Test Gradle Search**: Searches for "Gradle" and verifies results
5. **Test TestNG Search**: Searches for "TestNG" and verifies results

Each test:
- Navigates to Google.com
- Enters the search term
- Submits the search
- Verifies that expected results appear
- Supports both Chrome and Firefox browsers

## Configuration

### Environment Variables

- `JENKINS_URL`: Jenkins server URL
- `JENKINS_USERNAME`: Jenkins username
- `JENKINS_TOKEN`: Jenkins API token
- `BROWSER`: Browser to use (chrome/firefox)

### System Properties

- `jenkins.url`: Jenkins server URL
- `jenkins.username`: Jenkins username
- `jenkins.token`: Jenkins API token
- `jenkins.job`: Jenkins job name
- `browser`: Browser to use
- `csv.file`: Path to CSV test data file

## Troubleshooting

### Common Issues

1. **WebDriver not found**: Ensure browser drivers are in PATH or use WebDriverManager
2. **Jenkins connection failed**: Verify Jenkins URL, username, and API token
3. **Tests fail**: Check if Google.com is accessible and search results are as expected

### Logs

The application uses SLF4J with Logback for logging. Logs are written to console and can be configured in `logback.xml`.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
