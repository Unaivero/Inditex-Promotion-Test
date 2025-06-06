pipeline {
    agent any

    tools {
        maven 'Maven3' // Ensure 'Maven3' is configured in Jenkins Global Tool Configuration
        jdk 'JDK11'    // Ensure 'JDK11' is configured in Jenkins Global Tool Configuration
    }

    environment {
        // Define any environment variables needed for the build
        // EXAMPLE_VAR = 'example_value'
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout from Version Control System (e.g., Git)
                // Replace with your SCM configuration
                // Replace with your actual SCM (e.g., Git) configuration
                // Example for Git:
                // git url: 'https://github.com/your-username/InditexPromotionsTest.git', credentialsId: 'your-github-credentials-id', branch: 'main'
                echo 'Code checkout step - configure your SCM here.'
                // For this example, we'll assume the code is already available in the Jenkins workspace if not using SCM.
                // If you are running this Jenkinsfile locally or without a Git repo initially, 
                // you might comment out the 'git' line or use a local checkout command.
                echo 'Checked out code successfully (or assumed available in workspace).'
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    try {
                        // Clean, compile, and run tests
                        sh 'mvn clean test'
                    } catch (Exception e) {
                        echo "Build or tests failed: ${e.getMessage()}"
                        currentBuild.result = 'FAILURE'
                        throw e // Re-throw to ensure pipeline marks as failed
                    } finally {
                        // Always try to archive test results and publish Allure report
                        echo 'Archiving JUnit test results...'
                        junit '**/target/surefire-reports/TEST-*.xml' 
                        
                        echo 'Generating Allure report...'
                        allure includeProperties: false, jdk: 'JDK11', results: [[path: 'target/allure-results']]
                    }
                }
            }
        }

        // Optional: Add more stages like 'Deploy', 'Notify', etc.
        /*
        stage('Deploy') {
            when {
                branch 'main' // Only deploy from the main branch
                expression { currentBuild.result == 'SUCCESS' } // Only deploy if build and tests passed
            }
            steps {
                echo 'Deploying application...'
                // Add deployment steps here
            }
        }
        */

        stage('Notifications') {
            steps {
                script {
                    if (currentBuild.result == 'FAILURE') {
                        // Example: Send an email notification for failed builds
                        /*
                        mail to: 'your-email@example.com',
                             subject: "Jenkins Build Failed: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                             body: "Build failed. Check console output: ${env.BUILD_URL}"
                        */
                        echo "Build failed. Sending notification..."
                    } else {
                        echo "Build successful. No notification needed or send success notification."
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
            // Clean up workspace if needed
            // cleanWs()
        }
        success {
            echo 'Pipeline executed successfully.'
        }
        failure {
            echo 'Pipeline failed.'
        }
        unstable {
            echo 'Pipeline is unstable.'
        }
        changed {
            echo 'Pipeline state has changed.'
        }
    }
}
