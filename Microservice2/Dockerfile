FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/Microservice2-1.0.0-SNAPSHOT.jar /app/app.jar
RUN ls -la /app
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]