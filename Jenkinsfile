pipeline {
    agent any

    parameters {
        string(name: 'CSV_FILE', defaultValue: 'src/test/resources/testdata.csv', description: 'Path to CSV test data file')
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Browser to use for tests')
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
                bat "gradlew.bat test -Dbrowser=${params.BROWSER}"
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                    archiveArtifacts artifacts: 'build/reports/tests/test/**', allowEmptyArchive: true
                }
            }
        }

        stage('CSV Tests') {
            steps {
                bat "gradlew.bat runTestsWithCSV -Dcsv.file=${params.CSV_FILE} -Dbrowser=${params.BROWSER}"
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
