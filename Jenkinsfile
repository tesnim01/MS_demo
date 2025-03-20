pipeline {
    agent any
    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_REPO = 'mimo009/ms_demo_cicd'
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
                    docker build -t ${DOCKER_REPO}/eureka-server ./eureka-server
                    docker build -t ${DOCKER_REPO}/microservice1 ./Microservice1
                    docker build -t ${DOCKER_REPO}/microservice2 ./Microservice2
                    docker build -t ${DOCKER_REPO}/gateway ./Gateway
                '''
            }
        }
        
        stage('Push to DockerHub') {
            steps {
                sh '''
                    echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin
                    docker push ${DOCKER_REPO}/eureka-server
                    docker push ${DOCKER_REPO}/microservice1
                    docker push ${DOCKER_REPO}/microservice2
                    docker push ${DOCKER_REPO}/gateway
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