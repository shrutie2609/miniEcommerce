
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

        stage('Deploy Test Container') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'shop-db-password',
                        variable: 'DB_PASSWORD'
                    )
                ]) {
                    sh '''
                        docker rm -f shop-backend-cd-test || true
                        docker run -d \
                          --name shop-backend-cd-test \
                          --network ecommerce-mini_default \
                          -p 8086:8080 \
                          -e DB_URL="jdbc:mysql://mysql:3306/shopdb" \
                          -e DB_USER="root" \
                          -e DB_PASSWORD="$DB_PASSWORD" \
                          mini-ecommerce-backend:${BUILD_NUMBER}
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Build, tests, image creation, and deployment completed!'
        }

        failure {
            echo 'Pipeline failed. Check the Console Output.'
        }

        always {
            echo 'Jenkins pipeline execution finished.'
        }
    }
}
