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

    private lateinit var socketServerTestFixture: SocketServerTestFixture
    private val testMessage = "Hello from client".toByteArray()
    private val testResponse = testMessage.reversedArray()
    private var normalPort: Int = 0
    private var tlsPort: Int = 0

    @BeforeEach
    fun setUp() {
        normalPort = findFreePort()
        tlsPort = findFreePort()
        socketServerTestFixture = SocketServerTestFixture(tempDir)
        
        socketServerTestFixture.startTlsSocketServer(tlsPort) { message ->
            message.reversedArray()
        }
        socketServerTestFixture.startNormalSocketServer(normalPort) { message ->
            message.reversedArray()
        }
    }

    @AfterEach
    fun tearDown() {
        socketServerTestFixture.stopNormalServer()
        socketServerTestFixture.stopTlsServer()
    }

    @Test
    fun `should communicate via normal socket without tls`() =
        runBlocking {
            val tcpClientFactory = TcpClientFactory {}
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", normalPort)

            // Act
            val result = socket.send(testMessage)

            // Assert
            assertEquals(testResponse.decodeToString(), result.decodeToString())
        }

    @Test
    fun `should communicate via tls socket`() =
        runBlocking {
            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = socketServerTestFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = socketServerTestFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = socketServerTestFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = socketServerTestFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", tlsPort)

            // Act
            val result = socket.send(testMessage)

            // Assert
            assertEquals(testResponse.decodeToString(), result.decodeToString())
        }

    @Test
    fun `should echo back complete message via tls socket`() =
        runBlocking {
            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = socketServerTestFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = socketServerTestFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = socketServerTestFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = socketServerTestFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", tlsPort)

            // Act
            val result = socket.send(testMessage)

            // Assert
            assertEquals(testResponse.decodeToString(), result.decodeToString())
        }

    @Test
    fun `should handle multiple sequential tls connections`() =
        runBlocking {
            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = socketServerTestFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = socketServerTestFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = socketServerTestFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = socketServerTestFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", tlsPort)

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
    fun `should handle binary data over tls socket`() =
        runBlocking {
            val binaryMessage = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = socketServerTestFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = socketServerTestFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = socketServerTestFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = socketServerTestFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", tlsPort)

            // Act
            val result = socket.send(binaryMessage)

            // Assert
            val inverted = byteArrayOf(0x05, 0x04, 0x03, 0x02, 0x01)
            assertEquals(inverted.contentToString(), result.contentToString())
        }

    @Test
    fun `should handle large messages over tls socket`() =
        runBlocking {
            val largeMessage =
                ByteArray(10000) { i ->
                    (i % 256).toByte()
                }

            val tcpClientFactory =
                TcpClientFactory {
                    ssl {
                        keyStorePath = socketServerTestFixture.getSslConfiguration().keyStorePath
                        keyStorePassword = socketServerTestFixture.getSslConfiguration().keyStorePassword
                        trustStorePath = socketServerTestFixture.getSslConfiguration().trustStorePath
                        trustStorePassword = socketServerTestFixture.getSslConfiguration().trustStorePassword
                    }
                }
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", tlsPort)

            // Act
            val result = socket.send(largeMessage)

            // Assert
            assertEquals(largeMessage.reversedArray().contentToString(), result.contentToString())
        }
}
