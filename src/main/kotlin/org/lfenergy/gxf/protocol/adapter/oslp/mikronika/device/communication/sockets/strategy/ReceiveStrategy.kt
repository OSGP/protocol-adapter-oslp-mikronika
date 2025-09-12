// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.ByteArrayHelpers.Companion.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaKey
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.opensmartgridplatform.oslp.Oslp.Message

abstract class ReceiveStrategy(
    private val singingService: SigningService,
    private val mikronikaDeviceService: MikronikaDeviceService
) {
    private val logger = KotlinLogging.logger {}

    abstract fun handle(requestEnvelope: Envelope, mikronikaDevice: MikronikaDevice)

    abstract fun buildResponsePayload(requestEnvelope: Envelope, mikronikaDevice: MikronikaDevice): Message

    operator fun invoke(requestEnvelope: Envelope): Envelope? {
        val deviceUuid = String(requestEnvelope.deviceUid)
        val mikronikaDevice: MikronikaDevice = mikronikaDeviceService.findByDeviceUid(deviceUuid)

        if (!validateSignature(requestEnvelope, MikronikaKey(mikronikaDevice.publicKey))) return null
        handle(requestEnvelope, mikronikaDevice)
        val responsePayload = buildResponsePayload(requestEnvelope, mikronikaDevice).toByteArray()

        saveDeviceChanges(mikronikaDevice)
        return createResponseEnvelope(requestEnvelope, responsePayload)
    }

    private fun validateSignature(requestEnvelope: Envelope, key: MikronikaKey): Boolean {
        val verified =
            with(requestEnvelope) {
                singingService.verifySignature(
                    sequenceNumber.toByteArray(2) + deviceUid + lengthIndicator.toByteArray(2) + messageBytes,
                    securityKey,
                    key,
                )
            }

        if (!verified) {
            logger.error { "The signature is not valid for device ${requestEnvelope.deviceUid}!" }
            return false
        }
        return true
    }

    fun saveDeviceChanges(mikronikaDevice: MikronikaDevice) {
        mikronikaDeviceService.saveDevice(mikronikaDevice)
    }

    private fun createResponseEnvelope(
        requestEnvelope: Envelope,
        responsePayload: ByteArray,
    ): Envelope {
        val securityKey =
            singingService.createSignature(
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
