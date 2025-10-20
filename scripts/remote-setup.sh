#!/bin/bash

echo "Setting up Selenium Test Automation on Remote Jenkins..."

# Update system
sudo apt update && sudo apt upgrade -y

# Install Java 11
sudo apt install openjdk-11-jdk -y

# Install Chrome
wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" | sudo tee /etc/apt/sources.list.d/google-chrome.list
sudo apt update
sudo apt install -y google-chrome-stable

# Install Firefox
sudo apt install -y firefox

# Download ChromeDriver
CHROME_VERSION=$(google-chrome --version | awk '{print $3}' | cut -d. -f1-3)
CHROMEDRIVER_VERSION=$(curl -s "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_${CHROME_VERSION}")
wget -O chromedriver.zip "https://chromedriver.storage.googleapis.com/${CHROMEDRIVER_VERSION}/chromedriver_linux64.zip"
unzip chromedriver.zip
sudo mv chromedriver /usr/local/bin/
sudo chmod +x /usr/local/bin/chromedriver

# Download GeckoDriver
wget -O geckodriver.tar.gz "https://github.com/mozilla/geckodriver/releases/latest/download/geckodriver-v0.34.0-linux64.tar.gz"
tar -xzf geckodriver.tar.gz
sudo mv geckodriver /usr/local/bin/
sudo chmod +x /usr/local/bin/geckodriver

# Install Xvfb for headless testing
sudo apt install -y xvfb

echo "Setup completed!"
echo "Chrome version: $(google-chrome --version)"
echo "Firefox version: $(firefox --version)"
echo "ChromeDriver version: $(chromedriver --version)"
echo "GeckoDriver version: $(geckodriver --version)"
