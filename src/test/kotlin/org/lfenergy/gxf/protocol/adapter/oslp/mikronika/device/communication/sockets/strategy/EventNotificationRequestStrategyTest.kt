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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.mikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server.strategy.EventNotificationRequestStrategy
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceNotificationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceNotificationType
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.context.ApplicationEventPublisher
import java.time.format.DateTimeFormatter

@ExtendWith(MockKExtension::class)
class EventNotificationRequestStrategyTest {
    @MockK
    private lateinit var signingService: SigningService

    @MockK
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var eventNotificationRequestStrategy: EventNotificationRequestStrategy

    @Test
    fun `handle should set the sequence number from the request envelope to the mikronika device`() {
        val expectedSequenceNumber = 42

        val envelope = mockk<Envelope>(relaxed = true)
        val mikronikaDevice = mikronikaDevice()

        val message =
            Oslp.Message
                .newBuilder()
                .setEventNotificationRequest(
                    Oslp.EventNotificationRequest
                        .newBuilder()
                        .addNotifications(
                            Oslp.EventNotification
                                .newBuilder()
                                .setEvent(Oslp.Event.DIAG_EVENTS_GENERAL)
                                .setTimestamp(EVENT_DATETIME)
                                .setDescription(EVENT_DESCRIPTION),
                        ),
                ).build()

        every { envelope.sequenceNumber } returns expectedSequenceNumber
        every { envelope.message } returns message
        every { eventPublisher.publishEvent(any<DeviceNotificationReceivedEvent>()) } just runs

        eventNotificationRequestStrategy.handle(envelope, mikronikaDevice)

        assertThat(mikronikaDevice.sequenceNumber).isEqualTo(expectedSequenceNumber)

        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg { it: DeviceNotificationReceivedEvent ->
                    assertThat(it.deviceIdentification).isEqualTo(mikronikaDevice.deviceIdentification)
                    assertThat(it.eventType).isEqualTo(DeviceNotificationType.DIAG_EVENTS_GENERAL)
                    assertThat(it.description).isEqualTo(EVENT_DESCRIPTION)
                    assertThat(it.dateTime.format(formatter)).isEqualTo(EVENT_DATETIME)
                    assertThat(it.index).isNull()
                },
            )
        }
    }

    @Test
    fun `build response payload should return the correct event notification response`() {
        val envelope = mockk<Envelope>(relaxed = true)
        val mikronikaDevice = mikronikaDevice()

        val actualPayload = eventNotificationRequestStrategy.buildResponsePayload(envelope, mikronikaDevice)

        assertThat(actualPayload.hasEventNotificationResponse()).isTrue()

        val eventNotificationResponse = actualPayload.eventNotificationResponse

        assertThat(eventNotificationResponse.status)
            .isEqualTo(Oslp.Status.OK)
    }

    companion object {
        private const val EVENT_DESCRIPTION = "Test Event"
        private const val EVENT_DATETIME = "20250615123045"

        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }
}
