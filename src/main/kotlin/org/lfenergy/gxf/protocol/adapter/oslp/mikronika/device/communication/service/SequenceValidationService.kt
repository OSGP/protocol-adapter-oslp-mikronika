// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config.ValidationConfigurationProperties
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.springframework.stereotype.Service
import kotlin.math.abs
import kotlin.math.max

@Service
class SequenceValidationService(
    private val validationConfigurationProperties: ValidationConfigurationProperties,
) {
    fun checkSequenceNumber(
        currentSequenceNumber: Int?,
        receivedSequenceNumber: Int,
    ) {
        val maxSequence = validationConfigurationProperties.sequenceNumber.max
        val sequenceWindow = validationConfigurationProperties.sequenceNumber.window

        if (currentSequenceNumber == null) {
            throw InvalidRequestException("No current sequence number found for device")
        }

        if (receivedSequenceNumber !in 0..maxSequence) {
            throw InvalidRequestException("Received sequence number is not in valid range of 0 - $maxSequence")
        }

        var expectedSequenceNumber = currentSequenceNumber + 1

        if (expectedSequenceNumber > maxSequence) {
            expectedSequenceNumber = 0
        }

        if (isOutsideOfWindow(expectedSequenceNumber, receivedSequenceNumber, sequenceWindow, maxSequence)) {
            throw InvalidRequestException("Sequence number incorrect")
        }
    }

    private fun isOutsideOfWindow(
        expected: Int,
        received: Int,
        window: Int,
        max: Int,
    ): Boolean {
        val absDelta = abs(expected - received)
        return (absDelta > window) && (absDelta <= max - window)
    }
}
