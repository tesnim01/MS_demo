pipeline {
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-17'
            args '-v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/root/.m2'
        }
    }
    
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
                        -Dsonar.projectKey=ms_demo_parent \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.login=${SONAR_TOKEN}'
                }
            }
        }
        
        stage('Install Docker') {
            steps {
                sh '''
                    apt-get update
                    apt-get install -y apt-transport-https ca-certificates curl software-properties-common
                    curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
                    add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable"
                    apt-get update
                    apt-get install -y docker-ce docker-ce-cli containerd.io
                '''
            }
        }
        
        stage('Build Docker Images') {
            steps {
                sh '''
                    docker build -t ${DOCKER_REPO}/eureka-server:latest ./eureka-server
                    docker build -t ${DOCKER_REPO}/microservice1:latest ./Microservice1
                    docker build -t ${DOCKER_REPO}/microservice2:latest ./Microservice2
                    docker build -t ${DOCKER_REPO}/gateway:latest ./Gateway
                '''
            }
        }
        
        stage('Push to DockerHub') {
            steps {
                sh '''
                    echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin
                    docker push ${DOCKER_REPO}/eureka-server:latest
                    docker push ${DOCKER_REPO}/microservice1:latest
                    docker push ${DOCKER_REPO}/microservice2:latest
                    docker push ${DOCKER_REPO}/gateway:latest
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