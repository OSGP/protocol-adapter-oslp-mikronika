FROM gcr.io/distroless/java21-debian13:nonroot@sha256:e9a57bd6aed8e63e07f01349de5232a57f72d3f2f9409943a763ee50cbb119c1

WORKDIR /app

COPY build/libs/protocol-adapter-oslp-mikronika.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
