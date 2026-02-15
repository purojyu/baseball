ARG JAVA_VERSION
FROM eclipse-temurin:${JAVA_VERSION}-jdk

WORKDIR /app

EXPOSE 8080

CMD ["./gradlew", "bootRun"]
