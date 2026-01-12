// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organisation
import org.lfenergy.gxf.publiclighting.contracts.internal.audittrail.MessageType
import org.springframework.stereotype.Service

@Service
class AuditLoggingService(
    val auditLoggingClient: AuditLoggingClient,
) {
    fun logMessageFromDevice(
        device: Device,
        rawData: ByteArray,
    ) {
        logIncomingMessage(
            device,
            rawData,
        )
    }

    fun logReplyToDevice(
        device: Device,
        rawData: ByteArray,
    ) {
        logOutgoingMessage(
            device,
            rawData,
        )
    }

    fun logMessageToDevice(
        organisation: Organisation,
        device: Device,
        rawData: ByteArray,
    ) {
        logOutgoingMessage(
            device,
            rawData,
            organisation,
        )
    }

    fun logReplyFromDevice(
        organisation: Organisation,
        device: Device,
        rawData: ByteArray,
    ) {
        logIncomingMessage(
            device,
            rawData,
            organisation,
        )
    }

    private fun logOutgoingMessage(
        device: Device,
        rawData: ByteArray,
        organisation: Organisation? = null,
    ) {
        val auditLog =
            AuditLog(
                device,
                Message(
                    MessageType.TO_DEVICE,
                    rawData,
                ),
                organisation,
            )

        auditLoggingClient.sendLogItem(auditLog)
    }

    private fun logIncomingMessage(
        device: Device,
        rawData: ByteArray,
        organisation: Organisation? = null,
    ) {
        val auditLog =
            AuditLog(
                device,
                Message(
                    MessageType.FROM_DEVICE,
                    rawData,
                ),
                organisation,
            )

        auditLoggingClient.sendLogItem(auditLog)
    }
}

data class AuditLog(
    val device: Device,
    val message: Message,
    val organisation: Organisation? = null,
)

class Message(
    val messageType: MessageType,
    val rawData: ByteArray,
)
