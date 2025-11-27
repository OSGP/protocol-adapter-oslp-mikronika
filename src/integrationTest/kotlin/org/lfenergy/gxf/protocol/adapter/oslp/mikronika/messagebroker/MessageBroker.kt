// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.messagebroker

import jakarta.annotation.PostConstruct
import jakarta.jms.BytesMessage
import org.assertj.core.api.Assertions.assertThat
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.DEVICE_TYPE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_EVENTS_QUEUE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION_HEADER
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceEventMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class MessageBroker(
    val deviceNotificationJmsTemplate: JmsTemplate,
) {
    @PostConstruct
    fun initialize() {
        deviceNotificationJmsTemplate.receiveTimeout = 2000
    }

    fun receiveDeviceEventMessage(
        expectedDeviceIdentification: String,
        expectedEventType: EventType,
    ): DeviceEventMessage {
        val bytesMessage = deviceNotificationJmsTemplate.receive(DEVICE_EVENTS_QUEUE) as BytesMessage?

        assertThat(bytesMessage).isNotNull
        assertThat(bytesMessage!!.jmsType).isEqualTo(expectedEventType.name)
        assertThat(bytesMessage.getStringProperty(DEVICE_IDENTIFICATION_HEADER)).isEqualTo(expectedDeviceIdentification)

        val eventMessage = bytesMessage.toDeviceEventMessage()
        with(eventMessage.header) {
            assertThat(this).isNotNull
            assertThat(deviceIdentification).isEqualTo(expectedDeviceIdentification)
            assertThat(eventType).isEqualTo(expectedEventType)
            assertThat(deviceType).isEqualTo(DEVICE_TYPE)
        }
        return eventMessage
    }

    private fun BytesMessage.toDeviceEventMessage(): DeviceEventMessage {
        val bytes = ByteArray(this.bodyLength.toInt())
        this.readBytes(bytes)
        return DeviceEventMessage.parseFrom(bytes)
    }
}
