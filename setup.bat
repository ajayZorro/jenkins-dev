@echo off
echo Setting up Selenium Test Automation with Jenkins Integration...

REM Create logs directory
if not exist logs mkdir logs

REM Download ChromeDriver if not exists
if not exist chromedriver.exe (
    echo Downloading ChromeDriver...
    powershell -Command "Invoke-WebRequest -Uri 'https://chromedriver.storage.googleapis.com/LATEST_RELEASE' -OutFile 'chrome_version.txt'"
    for /f %%i in (chrome_version.txt) do set CHROME_VERSION=%%i
    powershell -Command "Invoke-WebRequest -Uri 'https://chromedriver.storage.googleapis.com/%CHROME_VERSION%/chromedriver_win32.zip' -OutFile 'chromedriver.zip'"
    powershell -Command "Expand-Archive -Path 'chromedriver.zip' -DestinationPath '.' -Force"
    del chrome_version.txt
    del chromedriver.zip
)

REM Download GeckoDriver if not exists
if not exist geckodriver.exe (
    echo Downloading GeckoDriver...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/mozilla/geckodriver/releases/latest/download/geckodriver-v0.34.0-win64.zip' -OutFile 'geckodriver.zip'"
    powershell -Command "Expand-Archive -Path 'geckodriver.zip' -DestinationPath '.' -Force"
    del geckodriver.zip
)

echo Setup completed!
echo.
echo Available commands:
echo 1. Run all tests: .\gradlew.bat test
echo 2. Run CSV tests: .\gradlew.bat runTestsWithCSV
echo 3. Trigger Jenkins: .\gradlew.bat triggerJenkinsJob
echo 4. Run scripts: scripts\run-tests.bat
echo.
echo For Jenkins integration, update the properties in scripts\run-tests.bat
