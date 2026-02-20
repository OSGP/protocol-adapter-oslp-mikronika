// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config.ValidationConfigurationProperties
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.mikronikaDevice

@ExtendWith(MockKExtension::class)
class SequenceValidationServiceTest {
    @MockK
    private lateinit var validationConfigurationProperties: ValidationConfigurationProperties

    @InjectMockKs
    private lateinit var subject: SequenceValidationService

    @BeforeEach
    fun setup() {
        every { validationConfigurationProperties.sequenceNumber } returns sequenceNumber
    }

    @Test
    fun `should throw InvalidRequestException when sequence number is out of window`() {
        val deviceSequence = 50
        val storedSequence = 1

        val mikronikaDevice = mikronikaDevice(storedSequence)

        assertThatThrownBy { subject.checkSequenceNumber(mikronikaDevice.sequenceNumber, deviceSequence) }
            .isInstanceOf(InvalidRequestException::class.java)
            .hasMessageContaining("Sequence number incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideSequence")
    fun `test validation passes for values`(
        receivedSequence: Int,
        deviceSequence: Int,
    ) {
        subject.checkSequenceNumber(deviceSequence, receivedSequence)
    }

    private val sequenceNumber =
        ValidationConfigurationProperties.SequenceNumber().apply {
            max = 65535
            window = 6
        }

    companion object {
        @JvmStatic
        fun provideSequence() =
            listOf(
                of(65529, 65534),
                of(65530, 65535),
                of(65531, 0),
                of(65532, 1),
                of(65533, 2),
                of(65534, 3),
                of(65535, 4),
                of(65534, 65529),
                of(65535, 65530),
                of(0, 65531),
                of(1, 65532),
                of(2, 65533),
                of(3, 65534),
                of(4, 65535),
            )
    }
}
