// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import java.io.FileInputStream
import java.net.InetSocketAddress
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class TcpClientFactory(
    val configuration: TcpClientConfiguration,
) {
    private val sslContext: SSLContext? by lazy {
        configuration.ssl?.let {
            createSslContext(it)
        }
    }

    fun createTcpClient(
        host: String,
        port: Int,
    ): JavaTcpClient = JavaTcpClient(InetSocketAddress(host, port), sslContext, configuration.proxy)

    private fun createSslContext(configuration: SslConfiguration): SSLContext {
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

    private fun getKeyStore(
        path: String,
        password: CharArray,
    ): KeyStore =
        KeyStore.getInstance("PKCS12").apply {
            FileInputStream(path).use { input ->
                load(input, password)
            }
        }
}

data class TcpClientConfiguration(
    val proxy: InetSocketAddress?,
    val ssl: SslConfiguration?,
)

data class SslConfiguration(
    val keyStorePath: String,
    val keyStorePassword: String,
    val trustStorePath: String,
    val trustStorePassword: String,
)

@DslMarker
annotation class TcpClientConfigurationDsl

@TcpClientConfigurationDsl
class TcpClientConfigurationBuilder {
    var proxy: InetSocketAddress? = null
    var ssl: SslConfiguration? = null

    fun proxy(block: DestinationConfigurationBuilder.() -> Unit) {
        proxy = DestinationConfigurationBuilder().apply(block).build()
    }

    fun ssl(block: SslConfigurationBuilder.() -> Unit) {
        ssl = SslConfigurationBuilder().apply(block).build()
    }

    fun build(): TcpClientConfiguration =
        TcpClientConfiguration(
            proxy = proxy,
            ssl = ssl,
        )
}

@TcpClientConfigurationDsl
class DestinationConfigurationBuilder {
    var host: String? = null
    var port: Int? = null

    fun build(): InetSocketAddress =
        InetSocketAddress(
            requireNotNull(host) { "destination host must be set" },
            requireNotNull(port) { "destination port must be set" },
        )
}

@TcpClientConfigurationDsl
class SslConfigurationBuilder {
    var keyStorePath: String? = null
    var keyStorePassword: String? = null
    var trustStorePath: String? = null
    var trustStorePassword: String? = null

    fun build(): SslConfiguration =
        SslConfiguration(
            keyStorePath = requireNotNull(keyStorePath) { "ssl.keyStorePath must be set" },
            keyStorePassword = requireNotNull(keyStorePassword) { "ssl.keyStorePassword must be set" },
            trustStorePath = requireNotNull(trustStorePath) { "ssl.trustStorePath must be set" },
            trustStorePassword = requireNotNull(trustStorePassword) { "ssl.trustStorePassword must be set" },
        )
}

fun tcpClientConfiguration(block: TcpClientConfigurationBuilder.() -> Unit): TcpClientConfiguration =
    TcpClientConfigurationBuilder().apply(block).build()
