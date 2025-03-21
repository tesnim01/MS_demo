pipeline {
    agent {
        node {
            label 'remote-builder'
        }
    }
    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        GITHUB_CREDENTIALS = credentials('github-credentials')
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_REPO = 'mimo009/ms_demo_cicd'
    }
    
    tools {
        maven 'Maven 3.9.6'
        jdk 'JDK 17'
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
                        withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                            sh '''
                                # Configure Git for pipeline commits
                                git config --global user.email "tesnimelbehi@gmail.com"
                                git config --global user.name "tesnim01"
                                git add .
                                git commit -m "[Pipeline] Update configuration - $(date)" || true
                                git remote set-url origin https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${GIT_USERNAME}/MS_demo.git
                                git push origin HEAD:main
                            '''
                        }
                    } catch (Exception e) {
                        echo "Git push failed: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
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
                    sh '''
                        # Add host.docker.internal to /etc/hosts if not present
                        if ! grep -q "host.docker.internal" /etc/hosts; then
                            echo "$(ip route | grep default | awk '{print $3}') host.docker.internal" | sudo tee -a /etc/hosts
                        fi
                        
                        mvn sonar:sonar \
                        -Dsonar.projectKey=ms_demo \
                        -Dsonar.host.url=http://host.docker.internal:9000 \
                        -Dsonar.login=${SONAR_TOKEN}
                    '''
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                sh '''
                    docker build -t ${DOCKER_REPO}:eureka-server eureka-server/
                    docker build -t ${DOCKER_REPO}:gateway Gateway/
                    docker build -t ${DOCKER_REPO}:microservice1 Microservice1/
                    docker build -t ${DOCKER_REPO}:microservice2 Microservice2/
                '''
            }
        }
        
        stage('Push to DockerHub') {
            steps {
                sh '''
                    docker login -u ${DOCKERHUB_CREDENTIALS_USR} -p ${DOCKERHUB_CREDENTIALS_PSW}
                    docker push ${DOCKER_REPO}:eureka-server
                    docker push ${DOCKER_REPO}:gateway
                    docker push ${DOCKER_REPO}:microservice1
                    docker push ${DOCKER_REPO}:microservice2
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
        failure {
            echo 'Pipeline failed!'
        }
    }
} 