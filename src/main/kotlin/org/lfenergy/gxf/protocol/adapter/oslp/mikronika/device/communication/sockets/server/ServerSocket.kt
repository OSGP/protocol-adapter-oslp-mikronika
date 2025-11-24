// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.springframework.stereotype.Component

@Component
class ServerSocket(
    private val serverSocketMessageProcessor: ServerSocketMessageProcessor,
) {
    @OptIn(DelicateCoroutinesApi::class)
    fun startListening(
        hostName: String,
        port: Int,
    ) {
        GlobalScope.launch {
            val serverSocket =
                aSocket(ActorSelectorManager(Dispatchers.IO))
                    .tcp()
                    .bind(InetSocketAddress(hostName, port))

            while (true) {
                val socket = serverSocket.accept()

                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)

                try {
                    val buffer = ByteArray(1024)
                    val bytesRead = input.readAvailable(buffer)

                    if (bytesRead > 0) {
                        val requestEnvelope = Envelope.parseFrom(buffer.copyOf(bytesRead))
                        serverSocketMessageProcessor.handleMessage(requestEnvelope, output)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    socket.close()
                }
            }
        }
    }
}
