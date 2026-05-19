FROM gcr.io/distroless/java21-debian13:nonroot@sha256:c56dc5813ba03ee893792c415eac200a2bcb733f26461f1f7236ee53c886f17a

WORKDIR /app

COPY build/libs/protocol-adapter-oslp-mikronika.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
