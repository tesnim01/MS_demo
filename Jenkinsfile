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
        GITHUB_CREDENTIALS = credentials('github-credentials')
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
        
        stage('Push to GitHub') {
            steps {
                script {
                    try {
                        sh '''
                            git config --global user.email "jenkins@example.com"
                            git config --global user.name "Jenkins"
                            git add .
                            git commit -m "Update docker-compose and Jenkins pipeline configuration"
                            git push https://${GITHUB_CREDENTIALS_USR}:${GITHUB_CREDENTIALS_PSW}@github.com/${GITHUB_CREDENTIALS_USR}/MS_demo.git HEAD:main
                        '''
                    } catch (Exception e) {
                        echo "No changes to commit or push failed: ${e.message}"
                    }
                }
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
                script {
                    try {
                        // Export the Docker repository for docker-compose to use
                        sh "export DOCKER_REPO=${DOCKER_REPO}"
                        
                        // Stop existing containers
                        sh 'docker-compose down --remove-orphans || true'
                        
                        // Pull latest images
                        sh 'docker-compose pull'
                        
                        // Start services in detached mode
                        sh 'docker-compose up -d'
                        
                        // Wait for services to be healthy
                        sh '''
                            echo "Waiting for services to be healthy..."
                            attempt=1
                            max_attempts=10
                            
                            until docker-compose ps | grep -q "healthy" || [ $attempt -gt $max_attempts ]
                            do
                                echo "Attempt $attempt of $max_attempts..."
                                sleep 30
                                attempt=$((attempt + 1))
                            done
                            
                            if [ $attempt -gt $max_attempts ]; then
                                echo "Services failed to become healthy"
                                docker-compose logs
                                exit 1
                            fi
                            
                            echo "All services are healthy!"
                            docker-compose ps
                        '''
                    } catch (Exception e) {
                        echo "Deployment failed: ${e.message}"
                        sh 'docker-compose logs'
                        throw e
                    }
                }
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