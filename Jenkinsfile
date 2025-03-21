pipeline {
    agent {
        node {
            label 'remote-builder'
        }
    }
    
    tools {
        maven 'Maven 3.9.6'
        jdk 'JDK 17'
    }
    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_REPO = 'mimo009/ms_demo_cicd'
        SONAR_HOST_URL = 'http://sonarqube:9000'
    }
    
    stages {
        stage('Verify Tools') {
            steps {
                sh '''
                    echo "Java version:"
                    java -version
                    echo "\nMaven version:"
                    mvn -version
                    echo "\nDocker version:"
                    docker --version
                    echo "\nWorking directory:"
                    pwd
                '''
            }
        }

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
                script {
                    try {
                        withSonarQubeEnv(credentialsId: 'sonar-token', installationName: 'SonarQube') {
                            sh 'mvn sonar:sonar -Dsonar.host.url=${SONAR_HOST_URL}'
                        }
                    } catch (Exception e) {
                        echo "SonarQube Analysis failed: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                sh '''
                    docker build -t ${DOCKER_REPO}:eureka-server eureka-server/
                    docker build -t ${DOCKER_REPO}:microservice1 Microservice1/
                    docker build -t ${DOCKER_REPO}:microservice2 Microservice2/
                    docker build -t ${DOCKER_REPO}:gateway Gateway/
                '''
            }
        }
        
        stage('Push to DockerHub') {
            steps {
                sh '''
                    echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin
                    docker push ${DOCKER_REPO}:eureka-server
                    docker push ${DOCKER_REPO}:microservice1
                    docker push ${DOCKER_REPO}:microservice2
                    docker push ${DOCKER_REPO}:gateway
                '''
            }
        }
        
        stage('Deploy with Docker Compose') {
            steps {
                sh '''
                    # Stop and remove existing containers
                    docker-compose down || true
                    
                    # Pull latest images
                    docker-compose pull
                    
                    # Start services
                    docker-compose up -d
                    
                    # Wait for services to be healthy
                    sleep 30
                    
                    # Check service status
                    docker-compose ps
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