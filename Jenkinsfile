pipeline {
    agent any
    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        SONAR_TOKEN = credentials('sonar-token')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar \
                        -Dsonar.projectKey=ms_demo \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.login=${SONAR_TOKEN}'
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                sh '''
                    docker build -t your-dockerhub-username/eureka-server:latest ./eureka-server
                    docker build -t your-dockerhub-username/microservice1:latest ./Microservice1
                    docker build -t your-dockerhub-username/microservice2:latest ./Microservice2
                    docker build -t your-dockerhub-username/gateway:latest ./Gateway
                '''
            }
        }
        
        stage('Push to DockerHub') {
            steps {
                sh '''
                    echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin
                    docker push your-dockerhub-username/eureka-server:latest
                    docker push your-dockerhub-username/microservice1:latest
                    docker push your-dockerhub-username/microservice2:latest
                    docker push your-dockerhub-username/gateway:latest
                '''
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
} 