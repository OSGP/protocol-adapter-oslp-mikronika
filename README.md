# Protocol Adapter OSLP Mikronika

A [GXF](https://github.com/OSGP) Spring Boot service that bridges OSLP (Open Street Light Protocol) device communication for Mikronika devices with the GXF platform over JMS (Apache ActiveMQ Artemis).

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Running Locally](#running-locally)
- [Configuration](#configuration)
  - [Device TCP Client](#device-tcp-client)
  - [TLS / SSL Setup](#tls--ssl-setup)
  - [JMS Broker](#jms-broker)
- [Building](#building)
- [Testing](#testing)
- [Docker](#docker)

---

## Overview

This adapter:

1. Consumes device command requests from a JMS queue (`gxf.publiclighting.oslp-mikronika.device-requests`).
2. Translates those requests into OSLP messages and sends them over TCP (optionally TLS) to a Mikronika device.
3. Translates the device response back and publishes the result to a JMS response queue (`gxf.publiclighting.oslp-mikronika.device-responses`).
4. Publishes device events and audit log entries to their respective queues.

---

## Architecture

```
GXF Platform
     │  JMS (Artemis)
     ▼
Protocol Adapter OSLP Mikronika
     │  TCP / mTLS (OSLP)
     ▼
Mikronika Device
```

Key components:

| Package | Responsibility |
|---|---|
| `command` | Maps and processes GXF commands (GetConfiguration, SetLight, etc.) |
| `device.communication` | TCP client (`JavaTcpClient`) with optional TLS and HTTP proxy support |
| `device.events` | Publishes device events received from Mikronika devices |
| `auditlogging` | Publishes audit log entries |
| `domain` | JPA entities and database access |

---

## Prerequisites

- Java 21
- Docker (for local dependencies)

---

## Running Locally

Start the required infrastructure (PostgreSQL + ActiveMQ Artemis) with Docker Compose:

```bash
docker compose up -d
```

Then run the application with the `dev` profile, which points to the local services:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The dev profile (`application-dev.yml`) configures:

- PostgreSQL at `localhost:51360`
- Artemis at `localhost:61616` (without TLS)

---

## Configuration

### Device TCP Client

The adapter connects to Mikronika devices using `device-tcp-client` settings in `application.yml`:

```yaml
device-tcp-client:
  device-port: 12125
  proxy:           # optional HTTP proxy
    host: localhost
    port: 1234
  ssl:             # optional — comment in to enable TLS to the device
    keyStorePath: ${DEVICE_TCP_CLIENT_KEY_STORE_PATH}
    keyStorePassword: ${DEVICE_TCP_CLIENT_KEY_STORE_PASSWORD}
    trustStorePath: ${DEVICE_TCP_CLIENT_TRUST_STORE_PATH}
    trustStorePassword: ${DEVICE_TCP_CLIENT_TRUST_STORE_PASSWORD}
```

When `ssl` is configured the client uses mTLS (mutual TLS), meaning both the client and the device verify each other's certificate.

### TLS / SSL Setup

Generate local adapter/device keystores and truststores with the helper script:

```bash
./scripts/generate-local-keystores.sh
```

Optional overrides:

```bash
STORE_PASSWORD=my-secret ./scripts/generate-local-keystores.sh
OUTPUT_DIR=./certs/custom ./scripts/generate-local-keystores.sh
VALIDITY_DAYS=365 ./scripts/generate-local-keystores.sh
```

The script creates these files in `./certs/local` by default:

- `adapter-keystore.jks`
- `adapter-truststore.jks`
- `device-keystore.jks`
- `device-truststore.jks`

The script output also prints export commands for `DEVICE_TCP_CLIENT_*` variables.

### JMS Broker

| Environment variable | Description |
|---|---|
| `JMS_KEY_STORE_PATH` | Path to the JMS client keystore |
| `JMS_KEY_STORE_PASSWORD` | JMS keystore password |
| `JMS_TRUST_STORE_PATH` | Path to the JMS client truststore |
| `JMS_TRUST_STORE_PASSWORD` | JMS truststore password |

---

## Building

```bash
# Build the project
./gradlew build

# Build without running tests
./gradlew build -x test -x integrationTest

# Run Spotless (lint / license headers)
./gradlew spotlessApply
```

---

## Testing

```bash
# Run unit tests
./gradlew test

# Run integration tests (requires Docker for Testcontainers)
./gradlew integrationTest

# Run all tests + coverage report
./gradlew aggregateTestCodeCoverageReport
```

Unit tests use JUnit 5 + MockK. Integration tests use Testcontainers (PostgreSQL, Artemis).

The TCP client TLS tests (`JavaTcpClientIntegrationTest`) run against an in-process test server and use a self-signed certificate generated at test time via `keytool`. They verify:

- Normal (plaintext) TCP communication
- One-way TLS communication
- Mutual TLS (mTLS) communication
- TLS handshake rejection when the client does not trust the server certificate
- TLS handshake rejection when the server does not trust the client certificate (mTLS enforcement)

---

## Docker

Build the image:

```bash
./gradlew bootJar
docker build -t protocol-adapter-oslp-mikronika .
```

Run the image:

```bash
docker run \
  -e JMS_KEY_STORE_PATH=/certs/jms-keystore.jks \
  -e JMS_KEY_STORE_PASSWORD=changeit \
  -e JMS_TRUST_STORE_PATH=/certs/jms-truststore.jks \
  -e JMS_TRUST_STORE_PASSWORD=changeit \
  -v /local/certs:/certs \
  protocol-adapter-oslp-mikronika
```

The image is based on `gcr.io/distroless/java21-debian13:nonroot` and runs as a non-root user.
