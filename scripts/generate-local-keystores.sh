#!/usr/bin/env bash
set -euo pipefail

# Generates local mTLS material for adapter and device using keytool.
# Password can be overridden: STORE_PASSWORD=my-secret ./scripts/generate-local-keystores.sh

STORE_PASSWORD="${STORE_PASSWORD:-changeit}"
VALIDITY_DAYS="${VALIDITY_DAYS:-3650}"
OUTPUT_DIR="${OUTPUT_DIR:-./certs/local}"

ADAPTER_ALIAS="adapter"
DEVICE_ALIAS="device"

ADAPTER_KEYSTORE="${OUTPUT_DIR}/adapter-keystore.jks"
ADAPTER_TRUSTSTORE="${OUTPUT_DIR}/adapter-truststore.jks"
ADAPTER_CERT="${OUTPUT_DIR}/adapter.cer"

DEVICE_KEYSTORE="${OUTPUT_DIR}/device-keystore.jks"
DEVICE_TRUSTSTORE="${OUTPUT_DIR}/device-truststore.jks"
DEVICE_CERT="${OUTPUT_DIR}/device.cer"

mkdir -p "${OUTPUT_DIR}"

# Clean old generated files to keep reruns deterministic.
rm -f \
  "${ADAPTER_KEYSTORE}" "${ADAPTER_TRUSTSTORE}" "${ADAPTER_CERT}" \
  "${DEVICE_KEYSTORE}" "${DEVICE_TRUSTSTORE}" "${DEVICE_CERT}"

# 1) Generate adapter identity
keytool -genkeypair \
  -alias "${ADAPTER_ALIAS}" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "${VALIDITY_DAYS}" \
  -storetype PKCS12 \
  -keystore "${ADAPTER_KEYSTORE}" \
  -storepass "${STORE_PASSWORD}" \
  -keypass "${STORE_PASSWORD}" \
  -dname "CN=localhost,OU=Dev,O=GXF,C=NL" \
  -ext SAN=dns:localhost,ip:127.0.0.1

# 2) Generate device identity
keytool -genkeypair \
  -alias "${DEVICE_ALIAS}" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "${VALIDITY_DAYS}" \
  -storetype PKCS12 \
  -keystore "${DEVICE_KEYSTORE}" \
  -storepass "${STORE_PASSWORD}" \
  -keypass "${STORE_PASSWORD}" \
  -dname "CN=device.local,OU=Dev,O=GXF,C=NL" \
  -ext SAN=dns:localhost,ip:127.0.0.1

# 3) Export both public certificates
keytool -exportcert \
  -alias "${ADAPTER_ALIAS}" \
  -keystore "${ADAPTER_KEYSTORE}" \
  -storepass "${STORE_PASSWORD}" \
  -file "${ADAPTER_CERT}"

keytool -exportcert \
  -alias "${DEVICE_ALIAS}" \
  -keystore "${DEVICE_KEYSTORE}" \
  -storepass "${STORE_PASSWORD}" \
  -file "${DEVICE_CERT}"

# 4) Build truststores (mutual trust)
keytool -importcert \
  -alias "${DEVICE_ALIAS}" \
  -file "${DEVICE_CERT}" \
  -storetype PKCS12 \
  -keystore "${ADAPTER_TRUSTSTORE}" \
  -storepass "${STORE_PASSWORD}" \
  -noprompt

keytool -importcert \
  -alias "${ADAPTER_ALIAS}" \
  -file "${ADAPTER_CERT}" \
  -storetype PKCS12 \
  -keystore "${DEVICE_TRUSTSTORE}" \
  -storepass "${STORE_PASSWORD}" \
  -noprompt

cat <<EOF

Generated local TLS material in: ${OUTPUT_DIR}

Adapter:
  Keystore   : ${ADAPTER_KEYSTORE}
  Truststore : ${ADAPTER_TRUSTSTORE}

Device:
  Keystore   : ${DEVICE_KEYSTORE}
  Truststore : ${DEVICE_TRUSTSTORE}

Password (all stores): ${STORE_PASSWORD}

Environment variables for adapter client:
  export DEVICE_TCP_CLIENT_KEY_STORE_PATH=${ADAPTER_KEYSTORE}
  export DEVICE_TCP_CLIENT_KEY_STORE_PASSWORD=${STORE_PASSWORD}
  export DEVICE_TCP_CLIENT_TRUST_STORE_PATH=${ADAPTER_TRUSTSTORE}
  export DEVICE_TCP_CLIENT_TRUST_STORE_PASSWORD=${STORE_PASSWORD}

EOF
