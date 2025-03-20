FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/Gateway-1.0.0-SNAPSHOT.jar /app/app.jar
RUN ls -la /app
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]