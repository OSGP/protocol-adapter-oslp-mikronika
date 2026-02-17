// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server.strategy

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging.AuditLoggingService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config.ValidationConfigurationProperties
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server.strategy.StrategyFactory.Companion.CONFIRM_REGISTER_DEVICE_STRATEGY
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.opensmartgridplatform.oslp.Oslp.Status
import org.springframework.stereotype.Component
import kotlin.math.abs

@Component(CONFIRM_REGISTER_DEVICE_STRATEGY)
class ConfirmRegisterDeviceStrategy(
    private val mikronikaDeviceService: MikronikaDeviceService,
    private val validationConfigurationProperties: ValidationConfigurationProperties,
    signingService: SigningService,
    auditLoggingService: AuditLoggingService,
) : ReceiveStrategy(signingService, mikronikaDeviceService, auditLoggingService) {
    private val logger = KotlinLogging.logger {}

    override fun handle(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ) {
        with(requestEnvelope.message.confirmRegisterDeviceRequest) {
            if (randomDevice != mikronikaDevice.randomDevice) {
                throw InvalidRequestException("Invalid randomDevice! Expected: ${mikronikaDevice.randomDevice} - Got: $randomDevice")
            }
            if (randomPlatform != mikronikaDevice.randomPlatform) {
                throw InvalidRequestException("Invalid randomPlatform! Expected: ${mikronikaDevice.randomPlatform} - Got: $randomPlatform")
            }

            checkAndUpdateSequenceNumber(mikronikaDevice, requestEnvelope.sequenceNumber)
        }
    }

    override fun buildResponsePayload(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ): Message {
        val response =
            Message
                .newBuilder()
                .setConfirmRegisterDeviceResponse(
                    Oslp.ConfirmRegisterDeviceResponse
                        .newBuilder()
                        .setRandomDevice(mikronikaDevice.randomDevice)
                        .setRandomPlatform(mikronikaDevice.randomPlatform)
                        .setSequenceWindow(validationConfigurationProperties.sequenceNumber.window)
                        .setStatusValue(Status.OK_VALUE)
                        .build(),
                ).build()
        return response
    }

    private fun checkAndUpdateSequenceNumber(
        mikronikaDevice: MikronikaDevice,
        receivedSequenceNumber: Int,
    ) {
        checkSequenceNumber(mikronikaDevice.sequenceNumber, receivedSequenceNumber)

        mikronikaDevice.sequenceNumber = receivedSequenceNumber
        mikronikaDeviceService.saveDevice(mikronikaDevice)
    }

    private fun checkSequenceNumber(
        currentSequenceNumber: Int?,
        receivedSequenceNumber: Int,
    ) {
        val maxSequence = validationConfigurationProperties.sequenceNumber.max
        val sequenceWindow = validationConfigurationProperties.sequenceNumber.window

        if (currentSequenceNumber == null) {
            logger.warn { "No current sequence number found for device" }
            throw InvalidRequestException("No current sequence number found for device")
        }
        val expectedSequenceNumber = (currentSequenceNumber + 1) % maxSequence

        val delta = abs(expectedSequenceNumber - receivedSequenceNumber)
        val valid = ((delta <= sequenceWindow) || (delta > (maxSequence - sequenceWindow)))

        if (!valid) {
            logger.warn { "Sequence number incorrect" }
            throw InvalidRequestException("Sequence number incorrect")
        }
    }
}
