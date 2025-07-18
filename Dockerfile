FROM alpine/java:21-jre

WORKDIR /app

COPY build/libs/protocol-adapter-oslp-mikronika.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
