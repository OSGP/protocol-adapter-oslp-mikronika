// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging

import com.google.protobuf.ByteString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.CommandIntegrationTest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.createHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.Device
import org.lfenergy.gxf.publiclighting.contracts.internal.audittrail.MessageType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.getConfigurationResponse
import org.opensmartgridplatform.oslp.message
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.activemq.ArtemisContainer

class AuditLoggingIntegrationTest : CommandIntegrationTest() {
    @Autowired
    private lateinit var artemisContainer: ArtemisContainer

    @BeforeEach
    @AfterEach
    fun clearQueues() {
//        artemisContainer.execInContainer("/bin/sh", "-c", "artemis", "queue", "purge", "--name", "gxf.publiclighting.oslp-mikronika.audit-log")
//        artemisContainer.execInContainer("/bin/sh", "-c", "artemis", "queue", "purge", "--name", "gxf.publiclighting.oslp-mikronika.device-responses")
    }

    @Test
    fun `should send log item messages when sending command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.GET_CONFIGURATION_REQUEST)
            }

        device.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        messageBroker
            .receiveLogItemMessage(
                DEVICE_IDENTIFICATION,
                MessageType.TO_DEVICE,
            ).also { logItemMessageToDevice ->
                assertNotNull(logItemMessageToDevice)
                assertEquals(MessageType.TO_DEVICE, logItemMessageToDevice.messageType)
                assertEquals("LianderNetManagement", logItemMessageToDevice.organizationIdentification)
                assertEquals(3, logItemMessageToDevice.rawDataSize)
                assertEquals(ByteString.fromHex("9A0200"), logItemMessageToDevice.rawData)
                assertEquals(true, logItemMessageToDevice.isValid)
            }

        messageBroker
            .receiveLogItemMessage(
                DEVICE_IDENTIFICATION,
                MessageType.FROM_DEVICE,
            ).also { logItemReplyFromDevice ->
                assertNotNull(logItemReplyFromDevice)
                assertEquals(MessageType.FROM_DEVICE, logItemReplyFromDevice.messageType)
                assertEquals("LianderNetManagement", logItemReplyFromDevice.organizationIdentification)
                assertEquals(7, logItemReplyFromDevice.rawDataSize)
                assertEquals(ByteString.fromHex("A2020408003002"), logItemReplyFromDevice.rawData)
                assertEquals(true, logItemReplyFromDevice.isValid)
            }
    }

    val okMock =
        Device.DeviceCallMock {
            message {
                getConfigurationResponse =
                    getConfigurationResponse {
                        status = Oslp.Status.OK
                        preferredLinkType = Oslp.LinkType.CDMA
                    }
            }
        }

    val rejectedMock =
        Device.DeviceCallMock {
            message {
                getConfigurationResponse =
                    getConfigurationResponse {
                        status = Oslp.Status.REJECTED
                    }
            }
        }
}
