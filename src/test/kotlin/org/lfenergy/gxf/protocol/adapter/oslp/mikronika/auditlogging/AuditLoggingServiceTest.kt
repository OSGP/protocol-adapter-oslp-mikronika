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
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.auditlogging.Direction

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
            testDecodedData,
        )

        verify {
            auditLoggingClientMock.sendLogItem(
                match {
                    it.device == testDevice &&
                        it.organization == null &&
                        it.message.direction == Direction.FROM_DEVICE &&
                        it.message.rawData.contentEquals(testRawData) &&
                        it.message.decodedData.contentEquals(testDecodedData)
                },
            )
        }
    }

    @Test
    fun testLogReplyToDevice() {
        auditLoggingService.logReplyToDevice(
            testDevice,
            testRawData,
            testDecodedData,
        )

        verify {
            auditLoggingClientMock.sendLogItem(
                match {
                    it.device == testDevice &&
                        it.organization == null &&
                        it.message.direction == Direction.TO_DEVICE &&
                        it.message.rawData.contentEquals(testRawData) &&
                        it.message.decodedData.contentEquals(testDecodedData)
                },
            )
        }
    }

    @Test
    fun testLogMessageToDevice() {
        auditLoggingService.logMessageToDevice(
            testOrganization,
            testDevice,
            testRawData,
            testDecodedData,
        )

        verify {
            auditLoggingClientMock.sendLogItem(
                match {
                    it.device == testDevice &&
                        it.organization == testOrganization &&
                        it.message.direction == Direction.TO_DEVICE &&
                        it.message.rawData.contentEquals(testRawData) &&
                        it.message.decodedData.contentEquals(testDecodedData)
                },
            )
        }
    }

    @Test
    fun testLogReplyFromDevice() {
        auditLoggingService.logReplyFromDevice(
            testOrganization,
            testDevice,
            testRawData,
            testDecodedData,
        )

        verify {
            auditLoggingClientMock.sendLogItem(
                match {
                    it.device == testDevice &&
                        it.organization == testOrganization &&
                        it.message.direction == Direction.FROM_DEVICE &&
                        it.message.rawData.contentEquals(testRawData) &&
                        it.message.decodedData.contentEquals(testDecodedData)
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
private val testDecodedData = "Decoded"

private val testOrganization =
    Organization(
        identification = "org123",
    )
