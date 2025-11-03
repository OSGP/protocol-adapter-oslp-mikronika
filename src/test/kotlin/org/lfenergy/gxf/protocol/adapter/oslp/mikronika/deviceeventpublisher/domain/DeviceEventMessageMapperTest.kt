// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceNotificationEventMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceNotificationEventMessageWithEmptyValues
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceNotificationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceNotificationReceivedEventWithEmptyValues
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceRegistrationEventMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceRegistrationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.domain.DeviceEventMessageMapper.toDeviceEventMessage

class DeviceEventMessageMapperTest {
    @Test
    fun `should convert notification event to device event message`() {
        // Arrange
        val event = deviceNotificationReceivedEvent
        val expectedMessage = deviceNotificationEventMessage

        // Act
        val actualMessage = event.toDeviceEventMessage()

        // Assert
        assertThat(actualMessage).usingRecursiveComparison().isEqualTo(expectedMessage)
    }

    @Test
    fun `should convert notification event to device event message with empty values`() {
        // Arrange
        val event = deviceNotificationReceivedEventWithEmptyValues
        val expectedMessage = deviceNotificationEventMessageWithEmptyValues

        // Act
        val actualMessage = event.toDeviceEventMessage()

        // Assert
        assertThat(actualMessage).usingRecursiveComparison().isEqualTo(expectedMessage)
    }

    @Test
    fun `should convert registration event to device event message`() {
        // Arrange
        val event = deviceRegistrationReceivedEvent
        val expectedMessage = deviceRegistrationEventMessage

        // Act
        val actualMessage = event.toDeviceEventMessage()

        // Assert
        assertThat(actualMessage).usingRecursiveComparison().isEqualTo(expectedMessage)
    }
}
