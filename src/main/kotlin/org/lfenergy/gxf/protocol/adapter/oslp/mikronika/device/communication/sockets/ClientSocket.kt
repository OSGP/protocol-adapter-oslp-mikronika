// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.exception.SendAndReceiveException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.service.DeviceStateService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.SendAndReceiveException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceStateService

class ClientSocket {
    private val logger = KotlinLogging.logger {}

    fun sendAndReceive(
        envelope: Envelope,
        clientAddress: String,
        clientPort: Int,
    ): Envelope =
        runBlocking(Dispatchers.IO) {
            val clientSocket: Socket =
                aSocket(ActorSelectorManager(Dispatchers.IO))
                    .tcp()
                    .connect(InetSocketAddress(clientAddress, clientPort))

            clientSocket.use {
                val output = it.openWriteChannel(autoFlush = true)
                val input = it.openReadChannel()

                val requestEnvelope: ByteArray = envelope.getBytes()

                logger.debug { "Request envelope to address and port: $clientAddress:$clientPort, Envelope: $envelope" }

                output.writeFully(requestEnvelope, 0, requestEnvelope.size)

                val buffer = ByteArray(1024)
                val bytesRead = input.readAvailable(buffer)

                if (bytesRead > 0) {
                    val deviceStateService = DeviceStateService.getInstance()

                    val responseEnvelope = Envelope.parseFrom(buffer.copyOf(bytesRead))
                    deviceStateService.updateSequenceNumber(responseEnvelope.sequenceNumber)

                    logger.debug { "Response envelope from address and port: $clientAddress:$clientPort, Envelope: $envelope" }

                    return@runBlocking responseEnvelope
                }
            }
            val exception =
                SendAndReceiveException("Error during Envelope send and receive operation to $clientAddress:$clientPort")
            logger.error { exception.toString() }
            throw exception
        }
}
