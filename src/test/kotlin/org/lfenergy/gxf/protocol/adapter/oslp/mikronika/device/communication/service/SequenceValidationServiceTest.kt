// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config.ValidationConfigurationProperties
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.mikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice

@ExtendWith(MockKExtension::class)
class SequenceValidationServiceTest {
    @MockK
    private lateinit var validationConfigurationProperties: ValidationConfigurationProperties

    @MockK
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

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

        assertThatThrownBy { subject.checkAndUpdateSequenceNumber(mikronikaDevice, deviceSequence) }
            .isInstanceOf(InvalidRequestException::class.java)
            .hasMessageContaining("Sequence number incorrect")
    }

    @Test
    fun `handle should correctly handle sequence number rollover from max to small value`() {
        val deviceSequence = 65535
        val receivedSequence = 3

        val mikronikaDevice = mikronikaDevice(sequenceNumber = deviceSequence)
        every { mikronikaDeviceService.saveDevice(mikronikaDevice) } returns mikronikaDevice

        subject.checkAndUpdateSequenceNumber(mikronikaDevice, receivedSequence)

        assertThat(mikronikaDevice.sequenceNumber).isEqualTo(receivedSequence)

        val mikronikaDeviceSlot = slot<MikronikaDevice>()
        verify { mikronikaDeviceService.saveDevice(capture(mikronikaDeviceSlot)) }

        val mikronikaDeviceCapture = mikronikaDeviceSlot.captured
        assertThat(mikronikaDeviceCapture.sequenceNumber).isEqualTo(receivedSequence)
    }

    @Test
    fun `handle should update the sequence number when random numbers match`() {
        val deviceSequence = 41
        val receivedSequence = 42

        val mikronikaDevice = mikronikaDevice(sequenceNumber = deviceSequence)
        every { mikronikaDeviceService.saveDevice(mikronikaDevice) } returns mikronikaDevice

        subject.checkAndUpdateSequenceNumber(mikronikaDevice, receivedSequence)

        assertThat(mikronikaDevice.sequenceNumber).isEqualTo(receivedSequence)

        val mikronikaDeviceSlot = slot<MikronikaDevice>()
        verify { mikronikaDeviceService.saveDevice(capture(mikronikaDeviceSlot)) }

        val mikronikaDeviceCapture = mikronikaDeviceSlot.captured
        assertThat(mikronikaDeviceCapture.sequenceNumber).isEqualTo(receivedSequence)
    }

    private val sequenceNumber =
        ValidationConfigurationProperties.SequenceNumber().apply {
            max = 65535
            window = 6
        }
}
