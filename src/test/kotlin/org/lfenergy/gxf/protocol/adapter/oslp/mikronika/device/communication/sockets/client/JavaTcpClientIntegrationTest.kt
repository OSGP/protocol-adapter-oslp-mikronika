// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.test.assertEquals

/**
 * Integration tests for JavaClientSocket that verify:
 * - Normal socket communication (raw TCP)
 * - SSL/TLS socket communication
 * - Proxied socket communication (with SSL)
 */
class JavaTcpClientIntegrationTest {
    @TempDir
    private lateinit var tempDir: File

    private lateinit var sslFixture: SocketTestFixture
    private val testMessage = "Hello from client".toByteArray()
    private val testResponse = "Hello from server".toByteArray()

    @BeforeEach
    fun setUp() {
        sslFixture = SocketTestFixture(tempDir)
    }

    @AfterEach
    fun tearDown() {
        sslFixture.stopServer()
    }

    @Test
    fun `should communicate via normal socket without ssl`() =
        runBlocking {
            val normalPort = findFreePort()
            // Arrange - start a non-TLS server
            startNormalSocketServer(normalPort) { _ ->
                testResponse
            }

            val tcpClientFactory = TcpClientFactory {}
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", normalPort)

            // Act
            val result = socket.send(testMessage)

            // Assert
            assertEquals(testResponse.decodeToString(), result.decodeToString())
        }

    @Test
    fun `should communicate via ssl socket`() =
        runBlocking {
            val sslPort = findFreePort()
            // Arrange - start SSL server
            sslFixture.startSslServer(sslPort) { message ->
                // Echo back with prefix
                ("Response: " + message.decodeToString()).toByteArray()
            }

            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = sslFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = sslFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = sslFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = sslFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", sslPort)

            // Act
            val result = socket.send(testMessage)

            // Assert
            assertEquals("Response: Hello from client", result.decodeToString())
        }

    @Test
    fun `should echo back complete message via ssl socket`() =
        runBlocking {
            val sslPort = findFreePort()
            // Arrange - start SSL server that echoes the exact message back
            sslFixture.startSslServer(sslPort) { message -> message }

            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = sslFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = sslFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = sslFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = sslFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", sslPort)

            // Act
            val result = socket.send(testMessage)

            // Assert
            assertEquals(testMessage.decodeToString(), result.decodeToString())
        }

    @Test
    fun `should handle multiple sequential ssl connections`() =
        runBlocking {
            val sslPort = findFreePort()
            // Arrange - start SSL server
            sslFixture.startSslServer(sslPort) { message ->
                message.reversedArray()
            }

            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = sslFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = sslFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = sslFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = sslFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", sslPort)

            // Act - send multiple messages
            val result1 = socket.send("Message 1".toByteArray())
            val result2 = socket.send("Message 2".toByteArray())
            val result3 = socket.send("Message 3".toByteArray())

            // Assert
            assertEquals("1 egasseM", result1.decodeToString())
            assertEquals("2 egasseM", result2.decodeToString())
            assertEquals("3 egasseM", result3.decodeToString())
        }

    @Test
    fun `should handle binary data over ssl socket`() =
        runBlocking {
            val sslPort = findFreePort()
            // Arrange - start SSL server that processes binary data
            sslFixture.startSslServer(sslPort) { message ->
                // Flip all bits in the message
                ByteArray(message.size) { i -> message[i].toInt().inv().toByte() }
            }

            val binaryMessage = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = sslFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = sslFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = sslFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = sslFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", sslPort)

            // Act
            val result = socket.send(binaryMessage)

            // Assert
            val inverted = ByteArray(binaryMessage.size) { i -> binaryMessage[i].toInt().inv().toByte() }
            assertEquals(inverted.contentToString(), result.contentToString())
        }

    @Test
    fun `should handle large messages over ssl socket`() =
        runBlocking {
            val sslPort = findFreePort()
            // Arrange - start SSL server
            sslFixture.startSslServer(sslPort) { message ->
                // Echo back the message
                message
            }

            val largeMessage =
                ByteArray(10000) { i ->
                    (i % 256).toByte()
                }

            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = sslFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = sslFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = sslFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = sslFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", sslPort)

            // Act
            val result = socket.send(largeMessage)

            // Assert
            assertEquals(largeMessage.contentToString(), result.contentToString())
        }

    private fun startNormalSocketServer(
        port: Int,
        messageHandler: (ByteArray) -> ByteArray,
    ) {
        Thread {
            try {
                ServerSocket(port, 1, InetAddress.getByName("127.0.0.1")).use { serverSocket ->
                    val clientSocket = serverSocket.accept()
                    clientSocket.use { socket ->
                        val inputStream = socket.inputStream
                        val outputStream = socket.outputStream

                        val buffer = ByteArray(4096)
                        val bytesRead = inputStream.read(buffer)

                        if (bytesRead > 0) {
                            val receivedMessage = buffer.sliceArray(0 until bytesRead)
                            val response = messageHandler(receivedMessage)
                            outputStream.write(response)
                            outputStream.flush()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        // Wait for server to start
        Thread.sleep(100)
    }

    private fun findFreePort(): Int = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { it.localPort }
}
