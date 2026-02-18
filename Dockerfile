FROM gcr.io/distroless/java21-debian13

WORKDIR /app

COPY build/libs/protocol-adapter-oslp-mikronika.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
