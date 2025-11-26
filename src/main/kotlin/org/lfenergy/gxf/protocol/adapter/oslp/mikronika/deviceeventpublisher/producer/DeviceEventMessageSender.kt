// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.producer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.config.DeviceEventsConfigurationProperties
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceEventMessage
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class DeviceEventMessageSender(
    val deviceNotificationJmsTemplate: JmsTemplate,
    val properties: DeviceEventsConfigurationProperties,
) {
    private val logger = KotlinLogging.logger {}

    fun send(message: DeviceEventMessage) {
        logger.info { "Sending device event message for device ${message.header.deviceIdentification} of type ${message.header.eventType}" }

        deviceNotificationJmsTemplate.send(properties.producer.outboundQueue) { session ->
            val msg = session.createBytesMessage()
            msg.jmsType = message.header.eventType.name
            msg.jmsCorrelationID = message.header.correlationUid
            msg.setStringProperty("DeviceIdentification", message.header.deviceIdentification)
            msg.writeBytes(message.toByteArray())
            msg
        }
    }
}
