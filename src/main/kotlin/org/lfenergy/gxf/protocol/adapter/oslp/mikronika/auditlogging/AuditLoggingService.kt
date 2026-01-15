// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.auditlogging.Direction
import org.lfenergy.gxf.publiclighting.contracts.internal.auditlogging.Direction.FROM_DEVICE
import org.lfenergy.gxf.publiclighting.contracts.internal.auditlogging.Direction.TO_DEVICE
import org.springframework.stereotype.Service

@Service
class AuditLoggingService(
    val auditLoggingClient: AuditLoggingClient,
) {
    fun logMessageFromDevice(
        device: Device,
        rawData: ByteArray,
        decodedData: String,
    ) {
        logIncomingMessage(
            device,
            rawData,
            decodedData,
        )
    }

    fun logReplyToDevice(
        device: Device,
        rawData: ByteArray,
        decodedData: String,
    ) {
        logOutgoingMessage(
            device,
            rawData,
            decodedData,
        )
    }

    fun logMessageToDevice(
        organization: Organization,
        device: Device,
        rawData: ByteArray,
        decodedData: String,
    ) {
        logOutgoingMessage(
            device,
            rawData,
            decodedData,
            organization,
        )
    }

    fun logReplyFromDevice(
        organization: Organization,
        device: Device,
        rawData: ByteArray,
        decodedData: String,
    ) {
        logIncomingMessage(
            device,
            rawData,
            decodedData,
            organization,
        )
    }

    private fun logOutgoingMessage(
        device: Device,
        rawData: ByteArray,
        decodedData: String,
        organization: Organization? = null,
    ) {
        val auditLog =
            AuditLog(
                device,
                Message(
                    TO_DEVICE,
                    rawData,
                    decodedData,
                ),
                organization,
            )

        auditLoggingClient.sendLogItem(auditLog)
    }

    private fun logIncomingMessage(
        device: Device,
        rawData: ByteArray,
        decodedData: String,
        organization: Organization? = null,
    ) {
        val auditLog =
            AuditLog(
                device,
                Message(
                    FROM_DEVICE,
                    rawData,
                    decodedData,
                ),
                organization,
            )

        auditLoggingClient.sendLogItem(auditLog)
    }
}

data class AuditLog(
    val device: Device,
    val message: Message,
    val organization: Organization? = null,
)

class Message(
    val direction: Direction,
    val rawData: ByteArray,
    val decodedData: String,
)
