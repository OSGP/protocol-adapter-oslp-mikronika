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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.DEVICE_TYPE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_UID
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
                    it.header.correlationUid == event.correlationUid
                    it.header.deviceIdentification == DEVICE_UID
                    it.header.deviceType == DEVICE_TYPE
                    it.header.eventType == EventType.DEVICE_NOTIFICATION
                    it.deviceNotificationReceivedEvent.notificationType.name == NOTIFICATION_TYPE
                    it.deviceNotificationReceivedEvent.description == NOTIFICATION_DESCRIPTION
                    it.deviceNotificationReceivedEvent.index == INDEX
                    it.deviceNotificationReceivedEvent.timestamp == timestamp
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
                    it.header.correlationUid == event.correlationUid
                    it.header.deviceIdentification == DEVICE_IDENTIFICATION
                    it.header.deviceType == DEVICE_TYPE
                    it.header.eventType == EventType.DEVICE_REGISTRATION
                    it.deviceRegistrationReceivedEvent.hasSchedule == HAS_SCHEDULE
                    it.deviceRegistrationReceivedEvent.networkAddress == NETWORK_ADDRESS
                },
            )
        }
    }
}
