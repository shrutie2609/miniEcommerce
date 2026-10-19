
pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                dir('backend') {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t mini-ecommerce-backend:${BUILD_NUMBER} ./backend'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check the Console Output for errors.'
        }

        always {
            echo 'Jenkins pipeline execution finished.'
        }
    }
}
