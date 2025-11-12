FROM eclipse-temurin:17-jdk-alpine
EXPOSE 8181
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} spring-docker-image.jar

ENTRYPOINT ["java", "-Dserver.address=0.0.0.0", "-Dserver.port=8181", "-jar","/spring-docker-image.jar"]
