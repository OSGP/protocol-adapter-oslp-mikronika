// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.net.SocketTimeoutException
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManagerFactory

class JavaClientSocket(
    configuration: ClientSocketConfigurationBuilder.() -> Unit,
) : ClientSocket(configuration) {
    override suspend fun send(bytes: ByteArray): ByteArray =
        withContext(Dispatchers.IO) {
            val sslContext =
                configuration.ssl?.let {
                    getSslContext(configuration.ssl)
                }

            val proxy =
                configuration.proxy?.let {
                    Proxy(
                        Proxy.Type.HTTP,
                        InetSocketAddress(it.host, it.port),
                    )
                } ?: Proxy.NO_PROXY

            Socket(proxy).use { rawSocket ->
                rawSocket.soTimeout = 2_000
                rawSocket.connect(InetSocketAddress(configuration.target.host, configuration.target.port), 5_000)

                if (sslContext != null) {
                    val sslSocket =
                        sslContext.socketFactory.createSocket(
                            rawSocket,
                            configuration.target.host,
                            configuration.target.port,
                            true,
                        ) as SSLSocket

                    sslSocket.use { socket ->
                        socket.startHandshake()

                        socket.outputStream.write(bytes)
                        socket.outputStream.flush()

                        return@withContext readUntilEofOrIdle(socket.inputStream)
                    }
                } else {
                    rawSocket.outputStream.write(bytes)
                    rawSocket.outputStream.flush()

                    return@withContext readUntilEofOrIdle(rawSocket.inputStream)
                }
            }
        }

    private fun readUntilEofOrIdle(input: InputStream): ByteArray {
        val buffer = ByteArray(4096)
        val result = ByteArrayOutputStream()

        while (true) {
            val read =
                try {
                    input.read(buffer)
                } catch (_: SocketTimeoutException) {
                    break
                }

            if (read == -1) break
            if (read > 0) result.write(buffer, 0, read)
        }

        return result.toByteArray()
    }

    fun getSslContext(configuration: SslConfiguration): SSLContext {
        val keyStore = getKeyStore(configuration.keyStorePath, configuration.keyStorePassword.toCharArray())
        val trustStore = getKeyStore(configuration.trustStorePath, configuration.trustStorePassword.toCharArray())

        val kmf =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                init(keyStore, configuration.keyStorePassword.toCharArray())
            }
        val tmf =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                init(trustStore)
            }

        val sslContext =
            SSLContext.getInstance("TLS").apply {
                init(kmf.keyManagers, tmf.trustManagers, SecureRandom())
            }

        return sslContext
    }

    fun getKeyStore(
        path: String,
        password: CharArray,
    ): KeyStore =
        KeyStore.getInstance("PKCS12").apply {
            FileInputStream(path).use { input ->
                load(input, password)
            }
        }
}
