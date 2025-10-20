#!/bin/bash

echo "Running Selenium Tests..."

# Set Jenkins properties (modify these values as needed)
export JENKINS_URL="http://localhost:8080"
export JENKINS_USERNAME="admin"
export JENKINS_TOKEN="your-api-token"
export JENKINS_JOB="selenium-tests"

# Run tests with CSV data
echo "Running tests with CSV data..."
./gradlew runTestsWithCSV

# Check if tests passed
if [ $? -eq 0 ]; then
    echo "Tests completed successfully!"
else
    echo "Tests failed!"
    exit 1
fi

# Trigger Jenkins job if tests passed
echo "Triggering Jenkins job..."
./gradlew triggerJenkinsJob -Djenkins.url=$JENKINS_URL -Djenkins.username=$JENKINS_USERNAME -Djenkins.token=$JENKINS_TOKEN -Djenkins.job=$JENKINS_JOB

echo "Script completed!"
