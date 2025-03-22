pipeline {
    agent {
        node {
            label 'remote-builder'
        }
    }
    
    environment {
        DOCKER_REPO = 'mimo009/ms_demo_cicd'
        SONAR_TOKEN = credentials('sonar-token')
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
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
                    echo "Maven version:"
                    mvn -version
                    echo "Docker version:"
                    docker --version
                    echo "Docker Compose version:"
                    docker-compose --version
                '''
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
                sh '''
                    echo "Current directory:"
                    pwd
                    echo "Directory contents:"
                    ls -la
                '''
            }
        }
        
        stage('Build and Test') {
            steps {
                sh '''
                    echo "Building eureka-server..."
                    cd eureka-server
                    mvn clean package -DskipTests
                    cd ..
                    
                    echo "Building gateway..."
                    cd Gateway
                    mvn clean package -DskipTests
                    cd ..
                    
                    echo "Building microservice1..."
                    cd Microservice1
                    mvn clean package -DskipTests
                    cd ..
                    
                    echo "Building microservice2..."
                    cd Microservice2
                    mvn clean package -DskipTests
                    cd ..
                '''
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        echo "Running SonarQube analysis..."
                        mvn clean verify sonar:sonar \
                            -Dsonar.projectKey=ms_demo \
                            -Dsonar.host.url=http://host.docker.internal:9000 \
                            -Dsonar.login=${SONAR_TOKEN} \
                            -Dsonar.java.source=17 \
                            -Dsonar.java.target=17
                    '''
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh '''
                        echo "Logging into DockerHub..."
                        echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin
                        
                        echo "Building eureka-server image..."
                        docker build -t ${DOCKER_REPO}:eureka-server eureka-server/
                        
                        echo "Building gateway image..."
                        docker build -t ${DOCKER_REPO}:gateway Gateway/
                        
                        echo "Building microservice1 image..."
                        docker build -t ${DOCKER_REPO}:microservice1 Microservice1/
                        
                        echo "Building microservice2 image..."
                        docker build -t ${DOCKER_REPO}:microservice2 Microservice2/
                    '''
                }
            }
        }
        
        stage('Push to DockerHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh '''
                        echo "Pushing images to DockerHub..."
                        docker push ${DOCKER_REPO}:eureka-server
                        docker push ${DOCKER_REPO}:gateway
                        docker push ${DOCKER_REPO}:microservice1
                        docker push ${DOCKER_REPO}:microservice2
                    '''
                }
            }
        }
        
        stage('Deploy with Docker Compose') {
            steps {
                script {
                    try {
                        sh '''
                            echo "Current directory:"
                            pwd
                            echo "Directory contents:"
                            ls -la
                            
                            # Stop existing containers
                            docker-compose down --remove-orphans || true
                            
                            # Pull latest images
                            docker-compose pull
                            
                            # Start services
                            docker-compose up -d
                            
                            # Wait for services to be healthy
                            echo "Waiting for services to be healthy..."
                            
                            # Function to check service health
                            check_health() {
                                local service=$1
                                local max_attempts=30
                                local attempt=1
                                
                                while [ $attempt -le $max_attempts ]; do
                                    echo "Checking $service health (Attempt $attempt/$max_attempts)..."
                                    
                                    # Show container status
                                    echo "Container status:"
                                    docker-compose ps $service
                                    
                                    # Show container logs
                                    echo "Container logs:"
                                    docker-compose logs --tail=50 $service
                                    
                                    # Check if container is running
                                    if ! docker-compose ps $service | grep -q "Up"; then
                                        echo "$service is not running!"
                                        docker-compose logs $service
                                        return 1
                                    fi
                                    
                                    # Check health status
                                    if docker-compose ps $service | grep -q "healthy"; then
                                        echo "$service is healthy!"
                                        return 0
                                    fi
                                    
                                    # If unhealthy, show detailed logs
                                    if docker-compose ps $service | grep -q "unhealthy"; then
                                        echo "$service is unhealthy. Full logs:"
                                        docker-compose logs $service
                                    fi
                                    
                                    sleep 10
                                    attempt=$((attempt + 1))
                                done
                                
                                echo "$service failed to become healthy"
                                return 1
                            }
                            
                            # Check each service in order
                            services=("eureka-server" "gateway" "microservice1" "microservice2")
                            
                            for service in "${services[@]}"; do
                                if ! check_health "$service"; then
                                    echo "Service $service failed health check"
                                    echo "All container statuses:"
                                    docker-compose ps
                                    echo "All container logs:"
                                    docker-compose logs
                                    exit 1
                                fi
                            done
                            
                            echo "All services are healthy!"
                            docker-compose ps
                        '''
                    } catch (Exception e) {
                        echo "Deployment failed: ${e.message}"
                        sh '''
                            echo "Container statuses:"
                            docker-compose ps
                            echo "Container logs:"
                            docker-compose logs
                        '''
                        throw e
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
            echo "Pipeline failed!"
        }
    }
} 