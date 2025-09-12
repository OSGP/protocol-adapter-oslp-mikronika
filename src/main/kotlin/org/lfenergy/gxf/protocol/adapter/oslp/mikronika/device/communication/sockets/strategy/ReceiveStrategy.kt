// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.ByteArrayHelpers.Companion.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.opensmartgridplatform.oslp.Oslp.Message

abstract class ReceiveStrategy(
    private val singingService: SigningService,
) {
    private val logger = KotlinLogging.logger {}

    abstract fun matches(message: Message): Boolean

    abstract fun handle(requestEnvelope: Envelope)

    abstract fun buildResponsePayload(requestEnvelope: Envelope): Message

    operator fun invoke(requestEnvelope: Envelope): Envelope? {
        if (!validateSignature(requestEnvelope)) return null
        handle(requestEnvelope)
        val responsePayload = buildResponsePayload(requestEnvelope).toByteArray()
        return createResponseEnvelope(requestEnvelope, responsePayload)
    }

    private fun validateSignature(requestEnvelope: Envelope): Boolean {
        val verified =
            with(requestEnvelope) {
                singingService.verifySignature(
                    sequenceNumber.toByteArray(2) + deviceUid + lengthIndicator.toByteArray(2) + messageBytes,
                    securityKey,
                    String(requestEnvelope.deviceUid, Charsets.UTF_8),
                )
            }

        if (!verified) {
            logger.error { "The signature is not valid for device ${requestEnvelope.deviceUid}!" }
            return false
        }
        return true
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
