// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device

import com.google.protobuf.ByteString
import com.gxf.utilities.oslp.message.signing.SigningUtil
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_UID
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.EVENT_DESCRIPTION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.EVENT_TIMESTAMP
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.EVENT_TYPE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.NETWORK_ADDRESS
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.RANDOM_DEVICE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.RANDOM_PLATFORM
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.SEQUENCE_NUMBER
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.encodedAsBase64
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config.ClientSocketConfigurationProperties
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server.ServerSocketConfiguration
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.Socket
import java.security.KeyPair
import java.util.concurrent.atomic.AtomicReference

@Component
class Device(
    private val signingUtil: SigningUtil,
    private val deviceKeyPair: KeyPair,
    private val serverSocketConfiguration: ServerSocketConfiguration,
    private val clientSocketConfiguration: ClientSocketConfigurationProperties,
) {
    val publicKey = deviceKeyPair.public.encodedAsBase64()

    fun sendDeviceRegistrationRequest(): Envelope {
        Socket(serverSocketConfiguration.hostName, serverSocketConfiguration.port).use { socket ->
            socket.getOutputStream().write(toEnvelope(deviceRegistrationRequestMessage()).getBytes())
            return Envelope.parseFrom(socket.getInputStream().readBytes())
        }
    }

    fun sendDeviceRegistrationConfirmationRequest(): Envelope {
        Socket(serverSocketConfiguration.hostName, serverSocketConfiguration.port).use { socket ->
            socket.getOutputStream().write(toEnvelope(deviceRegistrationConfirmationRequestMessage()).getBytes())
            return Envelope.parseFrom(socket.getInputStream().readBytes())
        }
    }

    fun sendEventNotificationRequest(): Envelope {
        eventNotificationRequestMessage()
        Socket(serverSocketConfiguration.hostName, serverSocketConfiguration.port).use { socket ->
            socket.getOutputStream().write(toEnvelope(eventNotificationRequestMessage()).getBytes())
            return Envelope.parseFrom(socket.getInputStream().readBytes())
        }
    }

    fun setupMock(mock: DeviceCallMock): Job = setupMocks(listOf(mock))

    @OptIn(DelicateCoroutinesApi::class)
    fun setupMocks(mocks: List<DeviceCallMock>): Job =
        GlobalScope.launch {
            val serverSocket =
                aSocket(ActorSelectorManager(Dispatchers.IO))
                    .tcp()
                    .bind(InetSocketAddress("localhost", clientSocketConfiguration.devicePort))

            for (mockedCall in mocks) {
                val socket = serverSocket.accept()

                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)

                try {
                    val buffer = ByteArray(1024)
                    val bytesRead = input.readAvailable(buffer)

                    if (bytesRead > 0) {
                        val requestEnvelope = Envelope.parseFrom(buffer.copyOf(bytesRead))
                        val responseMessage = mockedCall.handler(requestEnvelope)

                        val responseEnvelope = toEnvelope(responseMessage)

                        output.writeFully(responseEnvelope.getBytes())

                        mockedCall.capturedRequest.set(requestEnvelope)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    socket.close()
                }
            }
        }

    private fun deviceRegistrationRequestMessage() =
        Oslp.Message
            .newBuilder()
            .setRegisterDeviceRequest(
                Oslp.RegisterDeviceRequest
                    .newBuilder()
                    .setDeviceIdentification(DEVICE_IDENTIFICATION)
                    .setIpAddress(ByteString.copyFrom(InetAddress.getByName(NETWORK_ADDRESS).address))
                    .setHasSchedule(true)
                    .setRandomDevice(RANDOM_DEVICE)
                    .setDeviceType(Oslp.DeviceType.SSLD),
            ).build()

    private fun deviceRegistrationConfirmationRequestMessage() =
        Oslp.Message
            .newBuilder()
            .setConfirmRegisterDeviceRequest(
                Oslp.ConfirmRegisterDeviceRequest
                    .newBuilder()
                    .setRandomDevice(RANDOM_DEVICE)
                    .setRandomPlatform(RANDOM_PLATFORM),
            ).build()

    private fun eventNotificationRequestMessage() =
        Oslp.Message
            .newBuilder()
            .setEventNotificationRequest(
                Oslp.EventNotificationRequest
                    .newBuilder()
                    .addNotifications(
                        Oslp.EventNotification
                            .newBuilder()
                            .setEvent(Oslp.Event.valueOf(EVENT_TYPE))
                            .setTimestamp(EVENT_TIMESTAMP)
                            .setDescription(EVENT_DESCRIPTION)
                            .setIndex(ByteString.copyFrom("0".toByteArray()))
                            .build(),
                    ),
            ).build()

    private fun toEnvelope(message: Oslp.Message): Envelope {
        val payload = message.toByteArray()
        val sequenceNumber = SEQUENCE_NUMBER
        val deviceUid = DEVICE_UID
        val byteArray =
            sequenceNumber.toByteArray(2) +
                deviceUid.toByteArray() +
                payload.size.toByteArray(2) +
                payload

        val signature = signingUtil.createSignature(byteArray, deviceKeyPair.private)

        return Envelope(
            sequenceNumber = sequenceNumber,
            deviceUid = deviceUid.toByteArray(),
            lengthIndicator = payload.size,
            messageBytes = payload,
            securityKey = signature,
        )
    }

    data class DeviceCallMock(
        val handler: (requestEnvelope: Envelope) -> Oslp.Message,
    ) {
        val capturedRequest: AtomicReference<Envelope> = AtomicReference()
    }
}
