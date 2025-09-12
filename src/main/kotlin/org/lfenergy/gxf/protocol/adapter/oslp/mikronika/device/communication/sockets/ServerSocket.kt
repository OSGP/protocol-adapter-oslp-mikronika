// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets

import com.google.protobuf.InvalidProtocolBufferException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.ReceiveStrategy
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component

@Component
class ServerSocket(
    private val strategyFactory: StrategyFactory,
) {
    private val logger = KotlinLogging.logger {}

    @OptIn(DelicateCoroutinesApi::class)
    fun startListening(
        hostName: String,
        port: Int,
    ) {
        println("Starting the serversocket")
        GlobalScope.launch {
            val serverSocket =
                aSocket(ActorSelectorManager(Dispatchers.IO))
                    .tcp()
                    .bind(InetSocketAddress(hostName, port))
            logger.info { "Server is listening on address: ${serverSocket.localAddress}" }

            while (true) {
                val socket = serverSocket.accept()
                logger.info { "Accepted connection from ${socket.remoteAddress}" }

                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)

                try {
                    val buffer = ByteArray(1024)
                    val bytesRead = input.readAvailable(buffer)

                    if (bytesRead > 0) {
                        val requestEnvelope = Envelope.parseFrom(buffer.copyOf(bytesRead))

                        logger.info {
                            "Received: Seq: ${requestEnvelope.sequenceNumber} - Len: ${requestEnvelope.lengthIndicator} Message: ${requestEnvelope.message}"
                        }

                        val message = requestEnvelope.message;
                        getStrategyFor(message)?.let {
                            it.invoke(
                                requestEnvelope
                            )?.let { envelope ->
                                val responseBytes = envelope.getBytes()
                                output.writeFully(responseBytes)

                                logger.info {
                                    "Sent: Seq: ${envelope.sequenceNumber} - Len: ${envelope.lengthIndicator} Message: ${envelope.message}"
                                }
                            }
                        }
                    }
                } catch (e: InvalidProtocolBufferException) {
                    println("Failed to parse Protobuf message: ${e.message}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    logger.error(e.message ?: e.toString())
                } finally {
                    socket.close()
                }
            }
        }
    }

    fun getStrategyFor(message: Message): ReceiveStrategy? {
        with(message) {
            return when {
                hasRegisterDeviceRequest() -> strategyFactory.getStrategy("RegisterDeviceStrategy")
                hasConfirmRegisterDeviceRequest() -> strategyFactory.getStrategy("ConfirmRegisterDeviceStrategy")
                else -> error("Unexpected request message: $message")
            }
        }
    }
}
