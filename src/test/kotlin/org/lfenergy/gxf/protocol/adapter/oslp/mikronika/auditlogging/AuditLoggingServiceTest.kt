// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organisation
import org.lfenergy.gxf.publiclighting.contracts.internal.audittrail.MessageType

@ExtendWith(MockKExtension::class)
class AuditLoggingServiceTest {
    @MockK(relaxed = true)
    private lateinit var auditLoggingClientMock: AuditLoggingClient

    @InjectMockKs
    private lateinit var auditLoggingService: AuditLoggingService

    @Test
    fun testLogMessageFromDevice() {
        auditLoggingService.logMessageFromDevice(
            testDevice,
            testRawData,
        )

        verify {
            auditLoggingClientMock.sendLogItem(
                match {
                    it.device == testDevice &&
                        it.organisation == null &&
                        it.message.messageType == MessageType.FROM_DEVICE &&
                        it.message.rawData.contentEquals(testRawData)
                },
            )
        }
    }

    @Test
    fun testLogReplyToDevice() {
        auditLoggingService.logReplyToDevice(
            testDevice,
            testRawData,
        )

        verify {
            auditLoggingClientMock.sendLogItem(
                match {
                    it.device == testDevice &&
                        it.organisation == null &&
                        it.message.messageType == MessageType.TO_DEVICE &&
                        it.message.rawData.contentEquals(testRawData)
                },
            )
        }
    }

    @Test
    fun testLogMessageToDevice() {
        auditLoggingService.logMessageToDevice(
            testOrganisation,
            testDevice,
            testRawData,
        )

        verify {
            auditLoggingClientMock.sendLogItem(
                match {
                    it.device == testDevice &&
                        it.organisation == testOrganisation &&
                        it.message.messageType == MessageType.TO_DEVICE &&
                        it.message.rawData.contentEquals(testRawData)
                },
            )
        }
    }

    @Test
    fun testLogReplyFromDevice() {
        auditLoggingService.logReplyFromDevice(
            testOrganisation,
            testDevice,
            testRawData,
        )

        verify {
            auditLoggingClientMock.sendLogItem(
                match {
                    it.device == testDevice &&
                        it.organisation == testOrganisation &&
                        it.message.messageType == MessageType.FROM_DEVICE &&
                        it.message.rawData.contentEquals(testRawData)
                },
            )
        }
    }
}

private val testDevice =
    Device(
        deviceIdentification = "device123",
        networkAddress = "",
    )

private val testRawData = byteArrayOf(0x01, 0x02, 0x03)

private val testOrganisation =
    Organisation(
        organizationIdentification = "org123",
    )
