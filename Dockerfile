FROM gcr.io/distroless/java21-debian13:nonroot@sha256:035cbd34a779d91661c85319d7777a3f3aaba11f4a2dea96a327d546a197986b

WORKDIR /app

COPY build/libs/protocol-adapter-oslp-mikronika.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
