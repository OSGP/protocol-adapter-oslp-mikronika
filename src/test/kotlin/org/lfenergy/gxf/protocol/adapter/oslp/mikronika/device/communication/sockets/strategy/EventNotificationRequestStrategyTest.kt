package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.opensmartgridplatform.oslp.Oslp

@ExtendWith(MockKExtension::class)
class EventNotificationRequestStrategyTest {

    @MockK
    private lateinit var signingService: SigningService;

    @MockK
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

    @InjectMockKs
    private lateinit var eventNotificationRequestStrategy: EventNotificationRequestStrategy

    @Test
    fun `handle should set the sequence number from the request envelope to the mikronika device`() {
        val expectedSequenceNumber = 42

        val envelope = mockk<Envelope>(relaxed = true)
        val mikronikaDevice = mockk<MikronikaDevice>(relaxed = true)

        every { envelope.sequenceNumber } returns expectedSequenceNumber

        eventNotificationRequestStrategy.handle(envelope, mikronikaDevice)

        verify { mikronikaDevice.sequenceNumber = expectedSequenceNumber }
    }

    @Test
    fun `build response playload should return the correct event notification response`() {
        val envelope = mockk<Envelope>(relaxed = true)
        val mikronikaDevice = mockk<MikronikaDevice>(relaxed = true)

        val actualPayload = eventNotificationRequestStrategy.buildResponsePayload(envelope, mikronikaDevice)

        assertThat(actualPayload.hasEventNotificationResponse()).isTrue()

        val eventNotificationResponse = actualPayload.eventNotificationResponse

        assertThat(eventNotificationResponse.status)
            .isEqualTo(Oslp.Status.OK)
    }
}
