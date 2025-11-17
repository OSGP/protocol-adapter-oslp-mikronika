// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.messagebroker

import jakarta.annotation.PostConstruct
import jakarta.jms.BytesMessage
import org.assertj.core.api.Assertions.assertThat
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.DEVICE_TYPE
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceEventMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class MessageBroker(
    val jmsTemplate: JmsTemplate,
) {
    @PostConstruct
    fun initialize() {
        jmsTemplate.receiveTimeout = 2000
    }

    fun receiveDeviceEventMessage(
        deviceIdentification: String,
        eventType: EventType,
    ): DeviceEventMessage {
        val bytesMessage = jmsTemplate.receive("gxf.publiclighting.oslp-mikronika.device-events") as BytesMessage?

        assertThat(bytesMessage).isNotNull
        assertThat(bytesMessage!!.jmsType).isEqualTo(eventType.name)
        assertThat(bytesMessage.getStringProperty("DeviceIdentification")).isEqualTo(deviceIdentification)

        val eventMessage = bytesMessage.toDeviceEventMessage()
        with(eventMessage.header) {
            assertThat(this).isNotNull
            assertThat(deviceIdentification).isEqualTo(deviceIdentification)
            assertThat(eventType).isEqualTo(eventType)
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
