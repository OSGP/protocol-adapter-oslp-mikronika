// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server.strategy

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging.AuditLoggingService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaDevicePublicKey
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.opensmartgridplatform.oslp.Oslp.Message

abstract class ReceiveStrategy(
    private val signingService: SigningService,
    private val mikronikaDeviceService: MikronikaDeviceService,
    private val auditLoggingService: AuditLoggingService,
) {
    private val logger = KotlinLogging.logger {}

    abstract fun handle(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    )

    abstract fun buildResponsePayload(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ): Message

    operator fun invoke(requestEnvelope: Envelope): Envelope? {
        val deviceUid = String(requestEnvelope.deviceUid)
        val mikronikaDevice: MikronikaDevice = mikronikaDeviceService.findByDeviceUid(deviceUid)

        // TODO: [FDP-3625] Check sequence number for validity

        if (!validateSignature(requestEnvelope, MikronikaDevicePublicKey(mikronikaDevice.publicKey))) return null

        auditLoggingService.logMessageFromDevice(
            Device(mikronikaDevice.deviceIdentification),
            requestEnvelope.messageBytes,
            requestEnvelope.message.toString(),
        )

        try {
            handle(requestEnvelope, mikronikaDevice)
        } catch (e: InvalidRequestException) {
            logger.warn { "Invalid request received for deviceUid: ${mikronikaDevice.deviceUid} with message: ${e.message}" }
            return null
        }
        val responsePayload = buildResponsePayload(requestEnvelope, mikronikaDevice)

        auditLoggingService.logReplyToDevice(
            Device(mikronikaDevice.deviceIdentification),
            responsePayload.toByteArray(),
            responsePayload.toString(),
        )

        return finalizeInvocation(requestEnvelope, mikronikaDevice, responsePayload.toByteArray())
    }

    private fun finalizeInvocation(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
        responsePayload: ByteArray,
    ): Envelope {
        saveDeviceChanges(mikronikaDevice)
        return createResponseEnvelope(requestEnvelope, responsePayload)
    }

    private fun validateSignature(
        requestEnvelope: Envelope,
        verificationMikronikaDevicePublicKey: MikronikaDevicePublicKey,
    ): Boolean {
        val verified =
            with(requestEnvelope) {
                signingService.verifySignature(
                    sequenceNumber.toByteArray(2) + deviceUid + lengthIndicator.toByteArray(2) + messageBytes,
                    securityKey,
                    verificationMikronikaDevicePublicKey,
                )
            }

        if (!verified) {
            logger.error { "The signature is not valid for device ${requestEnvelope.deviceUid}!" }
            return false
        }
        return true
    }

    private fun saveDeviceChanges(mikronikaDevice: MikronikaDevice) {
        mikronikaDeviceService.saveDevice(mikronikaDevice)
    }

    private fun createResponseEnvelope(
        requestEnvelope: Envelope,
        responsePayload: ByteArray,
    ): Envelope {
        val securityKey =
            signingService.createSignature(
                requestEnvelope.sequenceNumber.toByteArray(2) +
                    requestEnvelope.deviceUid +
                    responsePayload.size.toByteArray(2) +
                    responsePayload,
            )

        return Envelope(
            securityKey,
            requestEnvelope.sequenceNumber,
            requestEnvelope.deviceUid,
            responsePayload.size,
            responsePayload,
        )
    }
}
