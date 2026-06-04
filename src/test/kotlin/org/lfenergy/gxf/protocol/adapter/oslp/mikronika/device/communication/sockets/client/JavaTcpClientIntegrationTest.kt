// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.net.ssl.SSLHandshakeException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Integration tests for JavaClientSocket that verify:
 * - Normal socket communication (raw TCP)
 * - SSL/TLS socket communication
 * - Proxied socket communication (with SSL)
 */
class JavaTcpClientIntegrationTest {
    private val testMessage = "Hello from client".toByteArray()
    private val testResponse = testMessage.reversedArray()

    private lateinit var tlsTcpClientFactory: TcpClientFactory
    private lateinit var sslTcpClient: TcpClient

    companion object {
        @TempDir
        private lateinit var tempDir: File

        private var normalPort: Int = 0
        private var tlsPort: Int = 0
        private lateinit var socketServerTestFixture: SocketServerTestFixture

        @JvmStatic
        @BeforeAll
        fun setUpAll() {
            socketServerTestFixture = SocketServerTestFixture(tempDir)

            tlsPort =
                socketServerTestFixture.startTlsSocketServer(requireClientAuth = true) { message ->
                    message.reversedArray()
                }
            normalPort =
                socketServerTestFixture.startNormalSocketServer { message ->
                    message.reversedArray()
                }
        }

        @JvmStatic
        @AfterAll
        fun tearDownAll() {
            socketServerTestFixture.stopNormalServer()
            socketServerTestFixture.stopTlsServer()
        }
    }

    @BeforeEach
    fun setUp() {
        tlsTcpClientFactory =
            TcpClientFactory(
                tcpClientConfiguration {
                    ssl {
                        initUsing(socketServerTestFixture.testSslStore)
                    }
                },
            )
        sslTcpClient = tlsTcpClientFactory.createTcpClient("127.0.0.1", tlsPort)
    }

    @Test
    fun `should communicate via normal socket without tls`() {
        runBlocking {
            val tcpClientFactory = TcpClientFactory(tcpClientConfiguration { })
            val socket = tcpClientFactory.createTcpClient("127.0.0.1", normalPort)

            // Act
            val result = socket.send(testMessage)

            // Assert
            assertEquals(testResponse.decodeToString(), result.decodeToString())
        }
    }

    fun SslConfigurationBuilder.initUsing(testSslStore: TestSslStore) {
        keyStorePath = testSslStore.keyStoreFile.absolutePath
        keyStorePassword = testSslStore.keyStorePassword
        trustStorePath = testSslStore.trustStoreFile.absolutePath
        trustStorePassword = testSslStore.trustStorePassword
    }

    fun SslConfigurationBuilder.initUsing(
        keyStore: TestSslStore,
        trustStore: TestSslStore,
    ) {
        keyStorePath = keyStore.keyStoreFile.absolutePath
        keyStorePassword = keyStore.keyStorePassword
        trustStorePath = trustStore.trustStoreFile.absolutePath
        trustStorePassword = trustStore.trustStorePassword
    }

    @Test
    fun `should communicate via tls socket`() {
        runBlocking {
            // Act
            val result = sslTcpClient.send(testMessage)

            // Assert
            assertEquals(testResponse.decodeToString(), result.decodeToString())
        }
    }

    @Test
    fun `should echo back complete message via tls socket`() {
        runBlocking {
            // Act
            val result = sslTcpClient.send(testMessage)

            // Assert
            assertEquals(testResponse.decodeToString(), result.decodeToString())
        }
    }

    @Test
    fun `should handle multiple sequential tls connections`() {
        runBlocking {
            // Act - send multiple messages
            val result1 = sslTcpClient.send("Message 1".toByteArray())
            val result2 = sslTcpClient.send("Message 2".toByteArray())
            val result3 = sslTcpClient.send("Message 3".toByteArray())

            // Assert
            assertEquals("1 egasseM", result1.decodeToString())
            assertEquals("2 egasseM", result2.decodeToString())
            assertEquals("3 egasseM", result3.decodeToString())
        }
    }

    @Test
    fun `should handle binary data over tls socket`() {
        runBlocking {
            // Act
            val binaryMessage = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
            val result = sslTcpClient.send(binaryMessage)

            // Assert
            val inverted = byteArrayOf(0x05, 0x04, 0x03, 0x02, 0x01)
            assertEquals(inverted.contentToString(), result.contentToString())
        }
    }

    @Test
    fun `should handle large messages over tls socket`() {
        runBlocking {
            // Act
            val largeMessage =
                ByteArray(10000) { i ->
                    (i % 256).toByte()
                }

            val result = sslTcpClient.send(largeMessage)

            // Assert
            assertEquals(largeMessage.reversedArray().contentToString(), result.contentToString())
        }
    }

    @Test
    fun `should fail tls handshake when client does not trust server certificate`() =
        runBlocking {
            val untrustedStore = TestSslStore(tempDir.resolve("untrusted-client-${System.nanoTime()}"))
            val untrustedClientFactory =
                TcpClientFactory(
                    tcpClientConfiguration {
                        ssl {
                            initUsing(untrustedStore)
                        }
                    },
                )
            val untrustedClient = untrustedClientFactory.createTcpClient("127.0.0.1", tlsPort)

            assertFailsWith<SSLHandshakeException> {
                untrustedClient.send(testMessage)
            }
        }

    @Test
    fun `should fail tls handshake when server does not trust client certificate`() =
        runBlocking {
            val untrustedClientKeyStore = TestSslStore(tempDir.resolve("untrusted-client-key-${System.nanoTime()}"))
            val clientFactoryWithUntrustedCertificate =
                TcpClientFactory(
                    tcpClientConfiguration {
                        ssl {
                            // Client trusts the server, but presents a certificate unknown to the server.
                            initUsing(
                                keyStore = untrustedClientKeyStore,
                                trustStore = socketServerTestFixture.testSslStore,
                            )
                        }
                    },
                )
            val clientWithUntrustedCertificate =
                clientFactoryWithUntrustedCertificate.createTcpClient("127.0.0.1", tlsPort)

            assertFailsWith<SSLHandshakeException> {
                clientWithUntrustedCertificate.send(testMessage)
            }
        }
}
