FROM maven:3.9-eclipse-temurin-25

WORKDIR /app

COPY pom.xml .
COPY src ./src

COPY src/main/resources/appsettings.docker.properties src/main/resources/appsettings.properties

RUN mvn clean package -DskipTests

ENTRYPOINT ["java", "-jar", "target/idk-1.0-SNAPSHOT.jar"]