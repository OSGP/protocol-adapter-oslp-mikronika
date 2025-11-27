// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config.ClientSocketConfigurationProperties
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaDevicePublicKey
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client.ClientSocket
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.stereotype.Component

@Component
class DeviceClientService(
    private val mikronikaDeviceService: MikronikaDeviceService,
    private val signingService: SigningService,
    private val socketProperties: ClientSocketConfigurationProperties,
) {
    private val logger = KotlinLogging.logger {}

    fun sendClientMessage(
        deviceRequest: DeviceRequest,
        responseMapper: (Envelope) -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val device = mikronikaDeviceService.findBydeviceIdentification(deviceRequest.deviceIdentification)

            val requestEnvelope = createEnvelope(device, deviceRequest.toOslpMessage())

            val sock = ClientSocket(deviceRequest.networkAddress, socketProperties.devicePort)

            val responseEnvelope = sock.sendAndReceive(requestEnvelope)

            if (!validateSignature(responseEnvelope, MikronikaDevicePublicKey(device.publicKey))) {
                logger.error { "Signature validation failed for message! DeviceUid: ${responseEnvelope.deviceUid}" }
                return@launch
            }

            device.sequenceNumber = responseEnvelope.sequenceNumber

            responseMapper.invoke(responseEnvelope)

            mikronikaDeviceService.saveDevice(device)
        }
    }

    private fun validateSignature(
        responseEnvelope: Envelope,
        verificationMikronikaDevicePublicKey: MikronikaDevicePublicKey,
    ): Boolean =
        with(responseEnvelope) {
            signingService.verifySignature(
                sequenceNumber.toByteArray(2) + deviceUid + lengthIndicator.toByteArray(2) + messageBytes,
                securityKey,
                verificationMikronikaDevicePublicKey,
            )
        }

    private fun createEnvelope(
        device: MikronikaDevice,
        payload: Oslp.Message,
    ): Envelope {
        val deviceUidBytes =
            device.deviceUid?.toByteArray()
                ?: throw IllegalArgumentException("DeviceUid is empty for device ${device.deviceIdentification}")

        val sequenceNumber =
            device.sequenceNumber
                ?: throw IllegalArgumentException("sequenceNumber is empty for device ${device.deviceIdentification}")

        val lengthIndicator = payload.serializedSize
        val messageBytes = payload.toByteArray()

        val signature =
            signingService.createSignature(
                sequenceNumber.toByteArray(2) +
                    deviceUidBytes +
                    lengthIndicator.toByteArray(2) +
                    messageBytes,
            )

        val envelope =
            Envelope(
                securityKey = signature,
                sequenceNumber = sequenceNumber,
                deviceUid = deviceUidBytes,
                lengthIndicator = lengthIndicator,
                messageBytes = messageBytes,
            )

        return envelope
    }
}
