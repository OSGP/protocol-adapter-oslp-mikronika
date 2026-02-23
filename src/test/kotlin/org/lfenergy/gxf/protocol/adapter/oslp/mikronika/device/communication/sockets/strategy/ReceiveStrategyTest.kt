// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging.AuditLoggingService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.mikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaDevicePublicKey
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.SequenceValidationService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server.strategy.EventNotificationRequestStrategy
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockKExtension::class)
class ReceiveStrategyTest {
    @MockK
    private lateinit var signingService: SigningService

    @MockK
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

    @MockK(relaxed = true)
    private lateinit var auditLoggingService: AuditLoggingService

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @MockK
    private lateinit var sequenceValidationService: SequenceValidationService

    @InjectMockKs
    private lateinit var eventNotificationRequestStrategy: EventNotificationRequestStrategy

    @BeforeEach
    fun setup() {
        every { sequenceValidationService.checkSequenceNumber(any(), any()) } just runs
    }

    @Test
    fun `invoke should throw an EntityNotFoundException when the device uid does not exist`() {
        val deviceUid = "unknown-device"
        val envelope = mockk<Envelope>()
        val expectedException = EntityNotFoundException("Device with identification $deviceUid not found")

        every { envelope.deviceUid } returns deviceUid.toByteArray()
        every { mikronikaDeviceService.findByDeviceUid(deviceUid) } throws expectedException

        assertThatThrownBy { eventNotificationRequestStrategy.invoke(envelope) }
            .isEqualTo(expectedException)
            .hasMessageContaining(expectedException.message)
    }

    @Test
    fun `invoke should return null when signature is invalid`() {
        val deviceUid = "device-uid"
        val envelope = mockEnvelope(deviceUid)

        val mikronikaDevice = mikronikaDevice()
        every { mikronikaDeviceService.findByDeviceUid(deviceUid) } returns mikronikaDevice

        every {
            signingService.verifySignature(
                any<ByteArray>(),
                any<ByteArray>(),
                any<MikronikaDevicePublicKey>(),
            )
        } returns false

        val result = eventNotificationRequestStrategy.invoke(envelope)

        assertThat(result).isNull()
    }

    @Test
    fun `handle should save the device and return a signed envelope`() {
        val deviceUid = "device-uid2"
        val envelope = mockEnvelope(deviceUid)
        val deviceSequence = 41

        val mikronikaDevice = mikronikaDevice(deviceSequence)
        every { mikronikaDeviceService.findByDeviceUid(deviceUid) } returns mikronikaDevice

        every {
            signingService.verifySignature(
                any<ByteArray>(),
                any<ByteArray>(),
                any<MikronikaDevicePublicKey>(),
            )
        } returns true
        every { mikronikaDeviceService.saveDevice(mikronikaDevice) } returns mikronikaDevice
        every { signingService.createSignature(any<ByteArray>()) } returns ByteArray(16) { 0x01 }

        val result = eventNotificationRequestStrategy.invoke(envelope)

        assertThat(result).isNotNull()

        verify(exactly = 1) {
            sequenceValidationService.checkSequenceNumber(deviceSequence, SEQUENCE_NUMBER)
            mikronikaDeviceService.saveDevice(mikronikaDevice)
            signingService.createSignature(any<ByteArray>())
        }
    }

    private fun mockEnvelope(deviceUid: String): Envelope {
        val envelope = mockk<Envelope>(relaxed = true)
        every { envelope.deviceUid } returns deviceUid.toByteArray()
        every { envelope.sequenceNumber } returns SEQUENCE_NUMBER

        return envelope
    }

    companion object {
        private const val SEQUENCE_NUMBER = 42
    }
}
