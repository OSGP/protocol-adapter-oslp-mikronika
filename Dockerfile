FROM gcr.io/distroless/java21-debian13:nonroot@sha256:258e48dcf7e9441095e8332c654e5005b21cd06f610ca9807ccbb56a5da412f7

WORKDIR /app

COPY build/libs/protocol-adapter-oslp-mikronika.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
