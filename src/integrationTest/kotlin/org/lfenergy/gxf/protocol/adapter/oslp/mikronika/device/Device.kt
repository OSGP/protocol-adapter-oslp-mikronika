// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device

import com.google.protobuf.ByteString
import com.gxf.utilities.oslp.message.signing.SigningUtil
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
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.configuration.ServerSocketConfiguration
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.Socket
import java.security.KeyPair

@Component
class Device(
    private val signingUtil: SigningUtil,
    private val deviceKeyPair: KeyPair,
    private val serverSocketConfiguration: ServerSocketConfiguration,
) {
    val publicKey = deviceKeyPair.public.encodedAsBase64()

    fun sendDeviceRegistrationRequest(): Envelope {
        Socket(serverSocketConfiguration.hostName, serverSocketConfiguration.port).use { socket ->
            socket.getOutputStream().write(requestEnvelope(deviceRegistrationRequestMessage()).getBytes())
            return Envelope.parseFrom(socket.getInputStream().readBytes())
        }
    }

    fun sendDeviceRegistrationConfirmationRequest(): Envelope {
        Socket(serverSocketConfiguration.hostName, serverSocketConfiguration.port).use { socket ->
            socket.getOutputStream().write(requestEnvelope(deviceRegistrationConfirmationRequestMessage()).getBytes())
            return Envelope.parseFrom(socket.getInputStream().readBytes())
        }
    }

    fun sendEventNotificationRequest(): Envelope {
        eventNotificationRequestMessage()
        Socket(serverSocketConfiguration.hostName, serverSocketConfiguration.port).use { socket ->
            socket.getOutputStream().write(requestEnvelope(eventNotificationRequestMessage()).getBytes())
            return Envelope.parseFrom(socket.getInputStream().readBytes())
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

    private fun requestEnvelope(message: Oslp.Message): Envelope {
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
}
