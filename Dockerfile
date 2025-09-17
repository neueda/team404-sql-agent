# Use a small, modern JDK base image
FROM eclipse-temurin:21-jdk

# App directory
WORKDIR /app

# Copy the built jar from your project (adjust name if needed)
ARG JAR_FILE=target/SQL_Agent-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY ${JAR_FILE} /app/app.jar
# note for devops what port to expose with -p
EXPOSE 8080
# If your Controller is in a package, replace 'Controller' below with the fully-qualified name
# e.g., com.example.Controller
ENTRYPOINT ["java", "-cp", "/app/app.jar", "Controller"]
