// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.producer

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.jms.BytesMessage
import jakarta.jms.Session
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.OUTBOUND_QUEUE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceNotificationEventMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.config.DeviceEventsConfigurationProperties
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class DeviceEventMessageSenderTest {
    @MockK
    lateinit var jmsTemplate: JmsTemplate

    @MockK
    lateinit var properties: DeviceEventsConfigurationProperties

    @InjectMockKs
    lateinit var messageSender: DeviceEventMessageSender

    @Test
    fun `should send device event message`() {
        // Arrange
        val message = deviceNotificationEventMessage
        val expectedBytes = message.toByteArray()

        val session: Session = mockk()
        val bytesMessage: BytesMessage = mockk()

        every { properties.producer.outboundQueue } returns OUTBOUND_QUEUE
        every { session.createBytesMessage() } returns bytesMessage
        justRun { bytesMessage.writeBytes(any<ByteArray>()) }
        justRun { bytesMessage.jmsType = any() }
        justRun { bytesMessage.jmsCorrelationID = any() }
        justRun { bytesMessage.setStringProperty(any(), any()) }
        justRun { jmsTemplate.send(any<String>(), any()) }

        // Act
        messageSender.send(message)

        // Assert
        val messageCreatorSlot = slot<MessageCreator>()
        verify(exactly = 1) { jmsTemplate.send(OUTBOUND_QUEUE, capture(messageCreatorSlot)) }

        val createdMessage = messageCreatorSlot.captured.createMessage(session)
        assertThat(createdMessage).isEqualTo(bytesMessage)

        verify {
            bytesMessage.writeBytes(match { it.contentEquals(expectedBytes) })
            bytesMessage.jmsType = EventType.DEVICE_NOTIFICATION.name
            bytesMessage.jmsCorrelationID = message.header.correlationUid
            bytesMessage.setStringProperty("DeviceIdentification", message.header.deviceIdentification)
        }
    }
}
