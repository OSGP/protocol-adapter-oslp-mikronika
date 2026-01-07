// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.messagebroker

import jakarta.annotation.PostConstruct
import jakarta.jms.BytesMessage
import org.assertj.core.api.Assertions.assertThat
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.DEVICE_TYPE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.AUDIT_LOG_QUEUE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_EVENTS_QUEUE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION_HEADER
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_REQUEST_QUEUE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_RESPONSE_QUEUE
import org.lfenergy.gxf.publiclighting.contracts.internal.audittrail.LogItemMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.audittrail.MessageType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceEventMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class MessageBroker(
    val deviceNotificationJmsTemplate: JmsTemplate,
    val deviceRequestJmsTemplate: JmsTemplate,
    val auditLoggingJmsTemplate: JmsTemplate,
) {
    @PostConstruct
    fun initialize() {
        deviceNotificationJmsTemplate.receiveTimeout = 2000
        deviceRequestJmsTemplate.receiveTimeout = 2000
        auditLoggingJmsTemplate.receiveTimeout = 2000
    }

    fun sendDeviceRequestMessage(request: DeviceRequestMessage) {
        deviceRequestJmsTemplate.send(DEVICE_REQUEST_QUEUE) { session ->
            val msg = session.createBytesMessage()
            msg.jmsType = request.header.requestType.name
            msg.jmsCorrelationID = request.header.correlationUid
            msg.setStringProperty("DeviceIdentification", request.header.deviceIdentification)
            msg.writeBytes(request.toByteArray())
            msg
        }
    }

    fun receiveDeviceResponseMessage(
        expectedDeviceIdentification: String,
        expectedResponseType: ResponseType,
    ): DeviceResponseMessage {
        val bytesMessage = deviceRequestJmsTemplate.receive(DEVICE_RESPONSE_QUEUE) as BytesMessage?

        assertThat(bytesMessage).isNotNull
        assertThat(bytesMessage!!.jmsType).isEqualTo(expectedResponseType.name)
        assertThat(bytesMessage.getStringProperty(DEVICE_IDENTIFICATION_HEADER)).isEqualTo(expectedDeviceIdentification)

        val responseMessage = bytesMessage.toDeviceResponseMessage()
        with(responseMessage.header) {
            assertThat(this).isNotNull
            assertThat(deviceIdentification).isEqualTo(expectedDeviceIdentification)
            assertThat(responseType).isEqualTo(expectedResponseType)
            assertThat(deviceType).isEqualTo(DEVICE_TYPE)
        }
        return responseMessage
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

    fun receiveLogItemMessage(
        expectedDeviceIdentification: String,
        expectedMessageType: MessageType,
    ): LogItemMessage {
        val bytesMessage = auditLoggingJmsTemplate.receive(AUDIT_LOG_QUEUE) as BytesMessage?
        assertThat(bytesMessage).isNotNull
        assertThat(bytesMessage!!.jmsType).isEqualTo(expectedMessageType.name)
        assertThat(bytesMessage.getStringProperty(DEVICE_IDENTIFICATION_HEADER)).isEqualTo(expectedDeviceIdentification)

        val logItemMessage = bytesMessage.toLogItemMessage()
        with(logItemMessage) {
            assertThat(this).isNotNull
            assertThat(deviceIdentification).isEqualTo(expectedDeviceIdentification)
            assertThat(messageType).isEqualTo(expectedMessageType)
        }
        return logItemMessage
    }

    private fun BytesMessage.toDeviceResponseMessage(): DeviceResponseMessage = DeviceResponseMessage.parseFrom(this.data())

    private fun BytesMessage.toDeviceEventMessage(): DeviceEventMessage = DeviceEventMessage.parseFrom(this.data())

    private fun BytesMessage.toLogItemMessage(): LogItemMessage = LogItemMessage.parseFrom(this.data())

    private fun BytesMessage.data(): ByteArray =
        ByteArray(this.bodyLength.toInt()).also {
            this.readBytes(it)
        }
}
