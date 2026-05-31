// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import java.io.ByteArrayOutputStream
import java.io.File
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.file.Files
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket

/**
 * Test fixture that provides:
 * - Generated test certificates
 * - SSL Socket Server for testing
 */
class SocketTestFixture(
    private val tempDir: File,
) {
    private val keyStorePath: String
    private val keyStorePassword = "keystorePassword"
    private val trustStorePath: String
    private val trustStorePassword = "truststorePassword"
    private var sslServerSocket: SSLServerSocket? = null
    private var serverThread: Thread? = null
    private var isRunning = false

    init {
        keyStorePath = tempDir.resolve("keystore.jks").absolutePath
        trustStorePath = tempDir.resolve("truststore.jks").absolutePath
        generateTestCertificates()
    }

    private fun generateTestCertificates() {
        // Create a self-signed certificate using Java's keytool
        val keystoreFile = File(keyStorePath)
        val truststoreFile = File(trustStorePath)

        // Generate keystore with self-signed certificate
        ProcessBuilder(
            "keytool",
            "-genkey",
            "-alias",
            "testkey",
            "-keyalg",
            "RSA",
            "-keysize",
            "2048",
            "-keystore",
            keystoreFile.absolutePath,
            "-storepass",
            keyStorePassword,
            "-keypass",
            keyStorePassword,
            "-dname",
            "CN=localhost,OU=Test,O=GXF,C=NL",
            "-validity",
            "365",
        ).start().waitFor()

        // Export certificate from keystore
        val certFile = File(tempDir, "test.cer")
        ProcessBuilder(
            "keytool",
            "-export",
            "-alias",
            "testkey",
            "-keystore",
            keystoreFile.absolutePath,
            "-storepass",
            keyStorePassword,
            "-file",
            certFile.absolutePath,
        ).start().waitFor()

        // Import certificate into truststore
        ProcessBuilder(
            "keytool",
            "-import",
            "-alias",
            "testkey",
            "-file",
            certFile.absolutePath,
            "-keystore",
            truststoreFile.absolutePath,
            "-storepass",
            trustStorePassword,
            "-noprompt",
        ).start().waitFor()
    }

    fun startSslServer(
        port: Int,
        messageHandler: (ByteArray) -> ByteArray,
    ) {
        require(!isRunning) { "Server is already running" }

        val keyStore =
            KeyStore.getInstance("PKCS12").apply {
                Files.newInputStream(File(keyStorePath).toPath()).use { input ->
                    load(input, keyStorePassword.toCharArray())
                }
            }

        val kmf =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                init(keyStore, keyStorePassword.toCharArray())
            }

        val sslContext =
            SSLContext.getInstance("TLS").apply {
                init(kmf.keyManagers, null, java.security.SecureRandom())
            }

        sslServerSocket =
            (sslContext.serverSocketFactory.createServerSocket(port) as SSLServerSocket).apply {
                needClientAuth = false
                enabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")
            }

        isRunning = true
        serverThread =
            Thread {
                try {
                    while (isRunning) {
                        val clientSocket = sslServerSocket?.accept() ?: return@Thread
                        Thread {
                            handleClient(clientSocket, messageHandler)
                        }.start()
                    }
                } catch (e: Exception) {
                    if (isRunning) {
                        e.printStackTrace()
                    }
                }
            }
        serverThread?.start()

        // Wait for server to be ready
        Thread.sleep(100)
    }

    private fun handleClient(
        clientSocket: Socket,
        messageHandler: (ByteArray) -> ByteArray,
    ) {
        try {
            clientSocket.use { socket ->
                socket.soTimeout = 200

                val outputStream = socket.outputStream

                val receivedMessage = readUntilIdle(socket)
                if (receivedMessage.isNotEmpty()) {
                    val response = messageHandler(receivedMessage)
                    outputStream.write(response)
                    outputStream.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readUntilIdle(socket: Socket): ByteArray {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(4096)

        while (true) {
            val read =
                try {
                    socket.inputStream.read(buffer)
                } catch (_: SocketTimeoutException) {
                    break
                }

            if (read == -1) break
            if (read > 0) result.write(buffer, 0, read)
        }

        return result.toByteArray()
    }

    fun stopServer() {
        isRunning = false
        sslServerSocket?.close()
        serverThread?.join(5000)
    }

    fun getSslConfiguration(): SslConfiguration =
        SslConfiguration(
            keyStorePath = keyStorePath,
            keyStorePassword = keyStorePassword,
            trustStorePath = trustStorePath,
            trustStorePassword = trustStorePassword,
        )
}
