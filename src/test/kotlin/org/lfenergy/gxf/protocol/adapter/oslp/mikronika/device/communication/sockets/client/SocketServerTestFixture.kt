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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket

/**
 * Test fixture that provides:
 * - Generated test certificates
 * - SSL Socket Server for testing
 */
class SocketServerTestFixture(
    tempDir: File,
    val testSslStore: TestSslStore = TestSslStore(tempDir),
) {
    private var tlsServerSocket: ServerSocket? = null
    private var tlsServerThread: Thread? = null
    private var tlsServerIsRunning = false

    private var normalServerSocket: ServerSocket? = null
    private var normalServerThread: Thread? = null
    private var normalServerIsRunning = false

    fun startNormalSocketServer(
        port: Int,
        messageHandler: (ByteArray) -> ByteArray,
    ) {
        require(!normalServerIsRunning) { "Normal server is already running" }
        normalServerIsRunning = true

        normalServerSocket = ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))

        normalServerThread =
            startSocketServer(
                normalServerSocket ?: error("Normal Server Socket should be configured"),
                messageHandler,
                { normalServerIsRunning },
            )
    }

    fun startTlsSocketServer(
        port: Int,
        requireClientAuth: Boolean = false,
        messageHandler: (ByteArray) -> ByteArray,
    ) {
        require(!tlsServerIsRunning) { "TLS server is already running" }
        tlsServerIsRunning = true

        val sslContext =
            SSLContext.getInstance("TLS").apply {
                val trustManagers = if (requireClientAuth) testSslStore.trustManagers else null
                init(testSslStore.keyManagers, trustManagers, java.security.SecureRandom())
            }

        tlsServerSocket =
            (sslContext.serverSocketFactory.createServerSocket(port) as SSLServerSocket).apply {
                needClientAuth = requireClientAuth
                enabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")
            }

        tlsServerThread =
            startSocketServer(
                tlsServerSocket ?: error("TLS Server Socket should be configured"),
                messageHandler,
                { tlsServerIsRunning },
            )
    }

    private fun startSocketServer(
        serverSocket: ServerSocket,
        messageHandler: (ByteArray) -> ByteArray,
        isRunning: () -> Boolean,
    ): Thread {
        val serverReady = CountDownLatch(1)

        return Thread {
            try {
                serverSocket.use { serverSocket ->
                    serverReady.countDown()
                    while (isRunning()) {
                        val clientSocket = serverSocket.accept()
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

            if(!serverReady.await(2, TimeUnit.SECONDS)) {
                error("Server socket did not bind within 2s")
            }
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
}

fun findFreePort(): Int = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { it.localPort }
