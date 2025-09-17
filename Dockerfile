FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=target/*dependencies.jar

COPY ${JAR_FILE} /app/app.jar

ENTRYPOINT ["java","-cp","/app/app.jar","Controller"]