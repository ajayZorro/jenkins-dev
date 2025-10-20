#!/bin/bash

echo "Setting up local Jenkins for POC testing..."

# Create Jenkins directory
mkdir -p jenkins-data

# Run Jenkins with Docker
echo "Starting Jenkins with Docker..."
docker run -d \
  --name jenkins-local \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins-data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

echo ""
echo "Jenkins is starting up..."
echo "Wait 2-3 minutes, then go to: http://localhost:8080"
echo ""
echo "Get initial password with:"
echo "docker exec jenkins-local cat /var/jenkins_home/secrets/initialAdminPassword"
echo ""
echo "Press Enter to continue..."
read
