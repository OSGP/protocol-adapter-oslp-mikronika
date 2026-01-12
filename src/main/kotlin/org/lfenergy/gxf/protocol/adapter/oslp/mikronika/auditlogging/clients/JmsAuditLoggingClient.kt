// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging.clients

import com.google.protobuf.kotlin.toByteString
import jakarta.jms.Session
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging.AuditLog
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging.AuditLoggingClient
import org.lfenergy.gxf.publiclighting.contracts.internal.audittrail.LogItemMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.audittrail.logItemMessage
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
internal class JmsAuditLoggingClient(
    val auditLoggingJmsTemplate: JmsTemplate,
) : AuditLoggingClient {
    override fun sendLogItem(auditLog: AuditLog) {
        auditLoggingJmsTemplate.send { session ->
            auditLog.toProtobuf().toJmsMessage(session)
        }
    }

    private fun AuditLog.toProtobuf() =
        logItemMessage {
            messageType = message.messageType
            organizationIdentification = organisation?.organizationIdentification ?: ""
            deviceIdentification = device.deviceIdentification
            rawData = message.rawData.toByteString()
            rawDataSize = message.rawData.size
        }

    private fun LogItemMessage.toJmsMessage(session: Session) =
        session.createBytesMessage().apply {
            jmsType = messageType.name
            setStringProperty("DeviceIdentification", deviceIdentification)
            writeBytes(toByteArray())
        }
}
