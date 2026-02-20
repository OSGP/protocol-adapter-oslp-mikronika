// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config.ValidationConfigurationProperties
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.springframework.stereotype.Service
import kotlin.math.abs

@Service
class SequenceValidationService(
    private val mikronikaDeviceService: MikronikaDeviceService,
    private val validationConfigurationProperties: ValidationConfigurationProperties,
) {
    private val logger = KotlinLogging.logger {}

    fun checkAndUpdateSequenceNumber(
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
