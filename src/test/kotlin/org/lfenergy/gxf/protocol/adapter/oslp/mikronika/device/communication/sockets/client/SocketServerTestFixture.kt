// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import java.io.ByteArrayOutputStream
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
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
class SocketServerTestFixture(
    private val tempDir: File,
) {
    private val keyStorePath: String
    private val keyStorePassword = "keystorePassword"
    private val trustStorePath: String
    private val trustStorePassword = "truststorePassword"

    private var tlsServerSocket: ServerSocket? = null
    private var tlsServerThread: Thread? = null
    private var tlsServerIsRunning = false

    private var normalServerSocket: ServerSocket? = null
    private var normalServerThread: Thread? = null
    private var normalServerIsRunning = false

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

    fun startNormalSocketServer(
        port: Int,
        messageHandler: (ByteArray) -> ByteArray,
    ) {
        require(!normalServerIsRunning) { "Normal server is already running" }
        normalServerIsRunning = true

        normalServerSocket = ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))

        normalServerThread = startSocketServer(
            normalServerSocket ?: error("Normal Server Socket should be configured"),
            messageHandler,
            { normalServerIsRunning }
        )
    }

    fun startTlsSocketServer(
        port: Int,
        messageHandler: (ByteArray) -> ByteArray,
    ) {
        require(!tlsServerIsRunning) { "TLS server is already running" }
        tlsServerIsRunning = true

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

        tlsServerSocket =
            (sslContext.serverSocketFactory.createServerSocket(port) as SSLServerSocket).apply {
                needClientAuth = false
                enabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")
            }

        tlsServerThread = startSocketServer(
            tlsServerSocket ?: error("TLS Server Socket should be configured"),
            messageHandler,
            { tlsServerIsRunning }
        )
    }

    private fun startSocketServer(
        serverSocket: ServerSocket,
        messageHandler: (ByteArray) -> ByteArray,
        isRunning: () -> Boolean,
    ): Thread {
        return Thread {
            try {
                serverSocket.use { serverSocket ->
                    while (isRunning()) {
                        val clientSocket = serverSocket?.accept() ?: return@Thread
                        Thread {
                            handleRequest(clientSocket, messageHandler)
                        }.start()
                    }
                }
            } catch (e: Exception) {
                if (isRunning()) {
                    e.printStackTrace()
                }
            }
        }.also {
            it.start()

            // Wait for server to start
            Thread.sleep(100)
        }
    }

    private fun handleRequest(
        clientSocket: Socket,
        messageHandler: (ByteArray) -> ByteArray,
    ) {
        try {
            clientSocket.use { socket ->
                socket.soTimeout = 200

                val outputStream = socket.outputStream

                val receivedBytes = socket.readUntilIdle()
                if (receivedBytes.isNotEmpty()) {
                    val response = messageHandler(receivedBytes)
                    outputStream.write(response)
                    outputStream.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Socket.readUntilIdle(): ByteArray {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(4096)

        while (true) {
            val read =
                try {
                    inputStream.read(buffer)
                } catch (_: SocketTimeoutException) {
                    break
                }

            if (read == -1) break
            if (read > 0) result.write(buffer, 0, read)
        }

        return result.toByteArray()
    }

    fun stopNormalServer() {
        normalServerIsRunning = false
        normalServerSocket?.close() // This is the only way to stop the clientSocket.accept to stop listening for new connections
        normalServerThread?.join(5000)
    }

    fun stopTlsServer() {
        tlsServerIsRunning = false
        tlsServerSocket?.close() // This is the only way to stop the clientSocket.accept to stop listening for new connections
        tlsServerThread?.join(5000)
    }

    fun getSslConfiguration(): SslConfiguration =
        SslConfiguration(
            keyStorePath = keyStorePath,
            keyStorePassword = keyStorePassword,
            trustStorePath = trustStorePath,
            trustStorePassword = trustStorePassword,
        )
}

fun findFreePort(): Int = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { it.localPort }
