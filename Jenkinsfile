pipeline {
    agent any

    parameters {
        string(name: 'TEST_METHODS', defaultValue: '', description: 'Test methods to run, space-separated. Leave empty to run all.')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Browser to use for tests')
        string(name: 'CSV_FILE', defaultValue: 'src/test/resources/testdata.csv', description: 'CSV test data file')
        string(name: 'JENKINS_URL', defaultValue: '', description: 'Optional: Jenkins URL to trigger downstream job')
        string(name: 'JENKINS_USERNAME', defaultValue: '', description: 'Optional: Jenkins Username')
        password(name: 'JENKINS_TOKEN', description: 'Optional: Jenkins API Token')
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Env') {
            steps {
                bat 'java -version'
                bat 'gradlew.bat --version'
            }
        }

        stage('Build') {
            steps {
                bat 'gradlew.bat clean build -x test'
            }
        }

        stage('TestNG Tests') {
            steps {
                script {
                    echo "Requested test methods: '${params.TEST_METHODS}'"
                    def gradleCmd = ''
                    if (params.TEST_METHODS?.trim()) {
                        // Create a temporary testng.xml with specific methods
                        def testMethods = params.TEST_METHODS.split()
                        def testngContent = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="Selenium Test Suite" parallel="methods" thread-count="2">
    <listeners>
        <listener class-name="com.jenkins.selenium.TestNGListener"/>
    </listeners>
    
    <test name="Google Search Tests">
        <classes>
            <class name="com.jenkins.selenium.SimpleGoogleTest">
                <methods>
${testMethods.collect { "                    <include name=\"${it}\"/>" }.join('\n')}
                </methods>
            </class>
        </classes>
    </test>
</suite>"""
                        
                        // Write temporary testng.xml
                        writeFile file: 'temp-testng.xml', text: testngContent
                        
                        // Use the temporary testng.xml
                        gradleCmd = "gradlew.bat test -Dtestng.suiteXmlFile=temp-testng.xml"
                    } else {
                        gradleCmd = 'gradlew.bat test'
                    }
                    echo "Executing command: ${gradleCmd}"
                    bat gradleCmd
                }
            }
            post {
                always {
                    // Archive test results directory
                    archiveArtifacts artifacts: 'build/test-results/**', allowEmptyArchive: true
                    
                    // Archive HTML test reports
                    archiveArtifacts artifacts: 'build/reports/tests/test/**', allowEmptyArchive: true
                    
                    // Archive screenshots (if they exist)
                    archiveArtifacts artifacts: 'build/screenshots/**', allowEmptyArchive: true
                    
                    // Archive test output directory
                    archiveArtifacts artifacts: 'build/test-output/**', allowEmptyArchive: true
                    
                    // Archive Allure results (if they exist)
                    archiveArtifacts artifacts: 'build/allure-results/**', allowEmptyArchive: true
                    
                    // Generate and publish Allure report
                    script {
                        try {
                            // Try to generate Allure report using Gradle task
                            bat 'gradlew.bat generateAllureReport'
                            
                            // Publish the generated report
                            allure([
                                includeProperties: false,
                                jdk: '',
                                properties: [],
                                reportBuildPolicy: 'ALWAYS',
                                results: [[path: 'build/allure-results']]
                            ])
                            echo "Allure report generated and published successfully"
                        } catch (Exception e) {
                            echo "Warning: Could not generate Allure report: ${e.message}"
                            echo "Allure results are still archived for manual review"
                        }
                    }
                    
                    // Publish HTML reports using HTML Publisher plugin
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Test Report'
                    ])
                }
            }
        }

        stage('Optional: Trigger Downstream') {
            when {
                allOf {
                    expression { return params.JENKINS_URL?.trim() }
                    expression { return params.JENKINS_USERNAME?.trim() }
                    expression { return params.JENKINS_TOKEN?.trim() }
                }
            }
            steps {
                bat "gradlew.bat triggerJenkinsJob -Djenkins.url=${params.JENKINS_URL} -Djenkins.username=${params.JENKINS_USERNAME} -Djenkins.token=${params.JENKINS_TOKEN}"
            }
        }
    }

    post {
        success { echo 'Pipeline completed successfully' }
        failure { echo 'Pipeline failed' }
    }
}
