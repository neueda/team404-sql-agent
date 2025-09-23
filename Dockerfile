FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=target/*dependencies.jar

COPY ${JAR_FILE} /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-cp","/app/app.jar","Main"]