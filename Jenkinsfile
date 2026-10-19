
pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out ecommerce project'
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
            echo 'Build, tests, and Docker image build completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the console output.'
        }
    }
}
