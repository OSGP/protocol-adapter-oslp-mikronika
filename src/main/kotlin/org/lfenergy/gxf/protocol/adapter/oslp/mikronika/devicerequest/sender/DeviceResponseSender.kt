// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.sender

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.config.DeviceRequestConfigurationProperties
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class DeviceResponseSender(
    val deviceRequestJmsTemplate: JmsTemplate,
    val properties: DeviceRequestConfigurationProperties,
) {
    private val logger = KotlinLogging.logger {}

    fun send(message: DeviceResponseMessage) {
        deviceRequestJmsTemplate.send(properties.producer.outboundQueue) { session ->
            val msg = session.createBytesMessage()
            msg.jmsType = message.header.responseType.name
            msg.jmsCorrelationID = message.header.correlationUid
            msg.setStringProperty("DeviceIdentification", message.header.deviceIdentification)
            msg.writeBytes(message.toByteArray())
            msg
        }
    }
}
