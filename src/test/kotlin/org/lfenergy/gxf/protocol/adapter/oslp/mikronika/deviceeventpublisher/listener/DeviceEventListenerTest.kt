// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.listener

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.DEVICE_TYPE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.HAS_SCHEDULE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.INDEX
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NETWORK_ADDRESS
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NOTIFICATION_DESCRIPTION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NOTIFICATION_TYPE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceNotificationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceRegistrationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.timestamp
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.producer.DeviceEventMessageSender
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceEventMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType

@ExtendWith(MockKExtension::class)
class DeviceEventListenerTest {
    @MockK
    lateinit var deviceEventMessageSender: DeviceEventMessageSender

    @InjectMockKs
    lateinit var deviceEventListener: DeviceEventListener

    @Test
    fun `should handle device notification event`() {
        // Arrange
        val event = deviceNotificationReceivedEvent
        every { deviceEventMessageSender.send(any<DeviceEventMessage>()) } just runs

        // Act
        deviceEventListener.handleDeviceEvent(event)

        // Assert
        verify(exactly = 1) {
            deviceEventMessageSender.send(
                withArg {
                    assertThat(it.header.correlationUid).isEqualTo(event.correlationUid)
                    assertThat(it.header.deviceIdentification).isEqualTo(DEVICE_IDENTIFICATION)
                    assertThat(it.header.deviceType).isEqualTo(DEVICE_TYPE)
                    assertThat(it.header.eventType).isEqualTo(EventType.DEVICE_NOTIFICATION)
                    assertThat(it.deviceNotificationReceivedEvent.notificationType.name).isEqualTo(NOTIFICATION_TYPE)
                    assertThat(it.deviceNotificationReceivedEvent.description).isEqualTo(NOTIFICATION_DESCRIPTION)
                    assertThat(it.deviceNotificationReceivedEvent.index).isEqualTo(INDEX)
                    assertThat(it.deviceNotificationReceivedEvent.timestamp).isEqualTo(timestamp)
                },
            )
        }
    }

    @Test
    fun `should handle device registration event`() {
        // Arrange
        val event = deviceRegistrationReceivedEvent
        every { deviceEventMessageSender.send(any<DeviceEventMessage>()) } just runs

        // Act
        deviceEventListener.handleDeviceEvent(event)

        // Assert
        verify(exactly = 1) {
            deviceEventMessageSender.send(
                withArg {
                    assertThat(it.header.correlationUid).isEqualTo(event.correlationUid)
                    assertThat(it.header.deviceIdentification).isEqualTo(DEVICE_IDENTIFICATION)
                    assertThat(it.header.deviceType).isEqualTo(DEVICE_TYPE)
                    assertThat(it.header.eventType).isEqualTo(EventType.DEVICE_REGISTRATION)
                    assertThat(it.deviceRegistrationReceivedEvent.hasSchedule).isEqualTo(HAS_SCHEDULE)
                    assertThat(it.deviceRegistrationReceivedEvent.networkAddress).isEqualTo(NETWORK_ADDRESS)
                },
            )
        }
    }
}
