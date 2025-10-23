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
                        gradleCmd = "gradlew.bat test " + params.TEST_METHODS.split().collect { "--tests \"${it}\"" }.join(' ')
                    } else {
                        gradleCmd = 'gradlew.bat test'
                    }
                    bat gradleCmd
                }
            }
            post {
                always {
                    // List test result files for debugging
                    script {
                        echo "Checking for test result files..."
                        if (fileExists('build/test-results/test/')) {
                            echo "Test results directory exists"
                            bat 'dir build\\test-results\\test\\*.xml /b 2>nul || echo No XML files found'
                        } else {
                            echo "Test results directory does not exist"
                        }
                    }
                    
                    // Publish JUnit test results with better error handling
                    script {
                        try {
                            junit 'build/test-results/test/*.xml'
                            echo "JUnit results published successfully"
                        } catch (Exception e) {
                            echo "Warning: Could not publish JUnit results: ${e.message}"
                            // Try alternative path
                            try {
                                junit 'build/test-results/**/*.xml'
                                echo "JUnit results published from alternative path"
                            } catch (Exception e2) {
                                echo "Warning: Could not publish JUnit results from alternative path: ${e2.message}"
                            }
                        }
                    }
                    
                    // Archive HTML test reports
                    archiveArtifacts artifacts: 'build/reports/tests/test/**', allowEmptyArchive: true
                    
                    // Archive Allure results (if they exist)
                    archiveArtifacts artifacts: 'build/allure-results/**', allowEmptyArchive: true
                    
                    // Archive screenshots (if they exist)
                    archiveArtifacts artifacts: 'build/screenshots/**', allowEmptyArchive: true
                    
                    // Archive test output directory
                    archiveArtifacts artifacts: 'build/test-output/**', allowEmptyArchive: true
                    
                    // Archive test results directory
                    archiveArtifacts artifacts: 'build/test-results/**', allowEmptyArchive: true
                    
                    // Publish Allure report (temporarily disabled until commandline is configured)
                    // allure([
                    //     includeProperties: false,
                    //     jdk: '',
                    //     properties: [],
                    //     reportBuildPolicy: 'ALWAYS',
                    //     results: [[path: 'build/allure-results']]
                    // ])
                    
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
