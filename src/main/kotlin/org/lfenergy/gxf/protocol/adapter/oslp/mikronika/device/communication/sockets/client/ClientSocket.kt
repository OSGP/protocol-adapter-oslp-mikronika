package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope

class ClientSocket(
    val clientAddress: String,
    val clientPort: Int,
) {
    suspend fun sendAndReceive(envelope: Envelope): Envelope {
        val clientSocket: Socket =
            aSocket(ActorSelectorManager(Dispatchers.IO))
                .tcp()
                .connect(InetSocketAddress(clientAddress, clientPort))

        clientSocket.use {
            val output = it.openWriteChannel(autoFlush = true)
            val input = it.openReadChannel()

            val requestEnvelope: ByteArray = envelope.getBytes()

            output.writeFully(requestEnvelope, 0, requestEnvelope.size)

            val buffer = ByteArray(1024)
            val bytesRead = input.readAvailable(buffer)

            if (bytesRead > 0) {
                val responseEnvelope = Envelope.parseFrom(buffer.copyOf(bytesRead))
//                    deviceStateService.updateSequenceNumber(responseEnvelope.sequenceNumber)
                return responseEnvelope
            }
        }
        throw Exception() // TODO: Decent exception handling
    }
}
