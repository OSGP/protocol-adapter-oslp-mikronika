// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets

import com.google.protobuf.InvalidProtocolBufferException
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.springframework.stereotype.Component

@Component
class ServerSocket(
    private val serverSocketMessageProcessor: ServerSocketMessageProcessor
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
                } catch (e: InvalidProtocolBufferException) {
                    e.printStackTrace()
                    println("Failed to parse Protobuf message: ${e.message}")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    socket.close()
                }
            }
        }
    }
}
