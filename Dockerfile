FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR file into the container
COPY target/authservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
