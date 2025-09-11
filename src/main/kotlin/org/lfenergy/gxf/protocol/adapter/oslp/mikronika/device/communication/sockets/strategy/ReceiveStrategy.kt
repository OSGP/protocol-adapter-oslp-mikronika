// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.oslp.domain.Envelope
import org.lfenergy.gxf.oslp.util.Logger
import org.lfenergy.gxf.oslp.util.SigningUtil
import org.lfenergy.gxf.oslp.util.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.helpers.ByteArrayHelpers.Companion.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.ByteArrayHelpers.Companion.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component


// TODO refactor stratergies implementation for mutiple devices
abstract class ReceiveStrategy(private val singingService: SigningService) {
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
                    sequenceNumber.toByteArray(2) + deviceId + lengthIndicator.toByteArray(2) + messageBytes,
                    securityKey,
                    requestEnvelope.deviceId.toString()
                )
            }

        if (!verified) {
            logger.error{"The signature is not valid for device ${requestEnvelope.deviceId}!"}
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
                    requestEnvelope.deviceId +
                    responsePayload.size.toByteArray(2) +
                    responsePayload,
            )

        return Envelope(
            securityKey,
            requestEnvelope.sequenceNumber,
            requestEnvelope.deviceId,
            responsePayload.size,
            responsePayload,
        )
    }

    companion object {
        fun getStrategyFor(message: Message): ReceiveStrategy {
            with(message) {
                return when {
                    hasRegisterDeviceRequest() -> RegisterDeviceStrategy(sign)
                    hasConfirmRegisterDeviceRequest() -> ConfirmRegisterDeviceStrategy()
                    hasEventNotificationRequest() -> EventNotificationRequestStrategy()
                    else -> error("Unexpected request message: $message")
                }
            }
        }
    }
}
