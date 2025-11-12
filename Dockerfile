FROM eclipse-temurin:17-jdk-alpine
EXPOSE 9999
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} spring-docker-image.jar

ENTRYPOINT ["java","-jar","/spring-docker-image.jar"]