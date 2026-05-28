// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

abstract class ClientSocket(
    configuration: ClientSocketConfigurationBuilder.() -> Unit,
) {
    internal val configuration = ClientSocketConfigurationBuilder().apply { configuration() }.build()

    abstract suspend fun send(bytes: ByteArray): ByteArray
}

data class ClientSocketConfiguration(
    val target: DestinationConfiguration,
    val proxy: DestinationConfiguration?,
    val ssl: SslConfiguration?,
)

data class DestinationConfiguration(
    val host: String,
    val port: Int,
)

data class SslConfiguration(
    val keyStorePath: String,
    val keyStorePassword: String,
    val trustStorePath: String,
    val trustStorePassword: String,
)

@DslMarker
annotation class ClientSocketConfigurationDsl

@ClientSocketConfigurationDsl
class ClientSocketConfigurationBuilder {
    var target: DestinationConfiguration? = null
    var proxy: DestinationConfiguration? = null
    var ssl: SslConfiguration? = null

    fun target(block: DestinationConfigurationBuilder.() -> Unit) {
        target = DestinationConfigurationBuilder().apply(block).build()
    }

    fun proxy(block: DestinationConfigurationBuilder.() -> Unit) {
        proxy = DestinationConfigurationBuilder().apply(block).build()
    }

    fun ssl(block: SslConfigurationBuilder.() -> Unit) {
        ssl = SslConfigurationBuilder().apply(block).build()
    }

    fun build(): ClientSocketConfiguration =
        ClientSocketConfiguration(
            target = requireNotNull(target) { "target needs to be specified for the socket" },
            proxy = proxy,
            ssl = ssl,
        )
}

@ClientSocketConfigurationDsl
class DestinationConfigurationBuilder {
    var host: String? = null
    var port: Int? = null

    fun build(): DestinationConfiguration =
        DestinationConfiguration(
            host = requireNotNull(host) { "destination host must be set" },
            port = requireNotNull(port) { "destination port must be set" },
        )
}

@ClientSocketConfigurationDsl
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

fun clientSocketConfiguration(block: ClientSocketConfigurationBuilder.() -> Unit): ClientSocketConfiguration =
    ClientSocketConfigurationBuilder().apply(block).build()
