// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import java.io.File
import java.nio.file.Files
import java.security.KeyStore
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

/**
 * Generates a keystore/truststore pair for TLS tests.
 */
class TestSslStore(
    val storeDir: File,
) {
    val keyStorePassword: String = "keystorePassword"
    val trustStorePassword: String = "truststorePassword"
    val keyStoreFile = storeDir.resolve("keystore.jks")
    val trustStoreFile = storeDir.resolve("truststore.jks")

    val keyManagers by lazy { loadKeyManagers() }
    val trustManagers by lazy { loadTrustManagers() }

    init {
        storeDir.mkdirs()
        generateTestCertificates()
    }

    private fun generateTestCertificates() {
        val certificateFile = storeDir.resolve("test.cer")

        // Create a self-signed certificate using Java's keytool
        runKeytool(
            "-genkey",
            "-alias",
            "testkey",
            "-keyalg",
            "RSA",
            "-keysize",
            "2048",
            "-keystore",
            keyStoreFile.absolutePath,
            "-storepass",
            keyStorePassword,
            "-keypass",
            keyStorePassword,
            "-dname",
            "CN=localhost,OU=Test,O=GXF,C=NL",
            "-validity",
            "365",
        )

        runKeytool(
            "-export",
            "-alias",
            "testkey",
            "-keystore",
            keyStoreFile.absolutePath,
            "-storepass",
            keyStorePassword,
            "-file",
            certificateFile.absolutePath,
        )

        runKeytool(
            "-import",
            "-alias",
            "testkey",
            "-file",
            certificateFile.absolutePath,
            "-keystore",
            trustStoreFile.absolutePath,
            "-storepass",
            trustStorePassword,
            "-noprompt",
        )
    }

    private fun runKeytool(vararg arguments: String) {
        val exitCode = ProcessBuilder("keytool", *arguments).start().waitFor()
        check(exitCode == 0) { "Failed to run keytool command: ${arguments.joinToString(" ")}" }
    }

    private fun loadKeyManagers(): Array<KeyManager> {
        val keyStore =
            KeyStore.getInstance("PKCS12").apply {
                Files.newInputStream(keyStoreFile.toPath()).use { input ->
                    load(input, keyStorePassword.toCharArray())
                }
            }

        val keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                init(keyStore, keyStorePassword.toCharArray())
            }

        return keyManagerFactory.keyManagers
    }

    private fun loadTrustManagers(): Array<TrustManager> {
        val trustStore =
            KeyStore.getInstance("PKCS12").apply {
                Files.newInputStream(trustStoreFile.toPath()).use { input ->
                    load(input, trustStorePassword.toCharArray())
                }
            }

        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                init(trustStore)
            }

        return trustManagerFactory.trustManagers
    }
}
