FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/eureka-server-1.0.0-SNAPSHOT.jar /app/app.jar
RUN ls -la /app
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]