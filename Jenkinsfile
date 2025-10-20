pipeline {
    agent {
        docker {
            // CloudBees CI controller/agents typically support Docker. This image has JDK + Chrome + Firefox preinstalled
            image 'cimg/openjdk:21.0-browsers'
            args '-u root:root' // allow installing extras if ever needed
            reuseNode true
        }
    }

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
                sh 'java -version || true'
                sh 'google-chrome --version || true'
                sh 'firefox --version || true'
                sh 'chmod +x gradlew'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('TestNG Tests') {
            steps {
                sh "./gradlew test -Dbrowser=${params.BROWSER}"
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
                sh "./gradlew runTestsWithCSV -Dcsv.file=${params.CSV_FILE} -Dbrowser=${params.BROWSER}"
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
                sh "./gradlew triggerJenkinsJob -Djenkins.url=${params.JENKINS_URL} -Djenkins.username=${params.JENKINS_USERNAME} -Djenkins.token=${params.JENKINS_TOKEN}"
            }
        }
    }

    post {
        success { echo 'Pipeline completed successfully' }
        failure { echo 'Pipeline failed' }
    }
}
