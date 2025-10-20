@echo off
echo Running Selenium Tests...

REM Set Jenkins properties (modify these values as needed)
set JENKINS_URL=http://localhost:8080
set JENKINS_USERNAME=admin
set JENKINS_TOKEN=your-api-token
set JENKINS_JOB=selenium-tests

REM Run tests with CSV data
echo Running tests with CSV data...
gradlew runTestsWithCSV

REM Check if tests passed
if %ERRORLEVEL% equ 0 (
    echo Tests completed successfully!
) else (
    echo Tests failed!
    exit /b 1
)

REM Trigger Jenkins job if tests passed
echo Triggering Jenkins job...
gradlew triggerJenkinsJob -Djenkins.url=%JENKINS_URL% -Djenkins.username=%JENKINS_USERNAME% -Djenkins.token=%JENKINS_TOKEN% -Djenkins.job=%JENKINS_JOB%

echo Script completed!
