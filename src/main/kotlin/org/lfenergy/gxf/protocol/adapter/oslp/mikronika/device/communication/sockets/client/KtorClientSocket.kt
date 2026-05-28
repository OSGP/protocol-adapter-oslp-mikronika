// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.ClientSocketException

class KtorClientSocket(
    configuration: ClientSocketConfigurationBuilder.() -> Unit,
) : ClientSocket(configuration) {
    override suspend fun send(bytes: ByteArray): ByteArray {
        val clientSocket: Socket =
            aSocket(ActorSelectorManager(Dispatchers.IO))
                .tcp()
                .connect(InetSocketAddress(configuration.target.host, configuration.target.port))

        clientSocket.use {
            val output = it.openWriteChannel(autoFlush = true)
            val input = it.openReadChannel()

            output.writeFully(bytes, 0, bytes.size)

            val buffer = ByteArray(1024)
            val bytesRead = input.readAvailable(buffer)

            when {
                bytesRead < 0 -> throw ClientSocketException("Connection was closed prematurely!")
                bytesRead == 0 -> throw ClientSocketException("No bytes received!")
            }

            return buffer.copyOf(bytesRead)
        }
    }
}
