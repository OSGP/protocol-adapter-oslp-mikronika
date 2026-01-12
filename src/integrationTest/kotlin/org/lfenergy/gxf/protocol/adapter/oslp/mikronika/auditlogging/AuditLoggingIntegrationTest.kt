// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging

import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.CommandIntegrationTest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.createHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.DeviceSimulator
import org.lfenergy.gxf.publiclighting.contracts.internal.audittrail.MessageType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.getConfigurationResponse
import org.opensmartgridplatform.oslp.message

class AuditLoggingIntegrationTest : CommandIntegrationTest() {
    @Test
    fun `should send log item messages when sending command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.GET_CONFIGURATION_REQUEST)
            }

        deviceSimulator.addMock(okMock)
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
            }
    }

    @Test
    fun `should send log item messages when receiving device event`() {
        val expectedReceivedRawMessage = "8a012a0a2808001201301a114a75737420612074657374206576656e74220e3230323531313137313031353330"

        deviceSimulator.sendEventNotificationRequest()
        messageBroker.receiveDeviceEventMessage(DEVICE_IDENTIFICATION, EventType.DEVICE_NOTIFICATION)

        messageBroker
            .receiveLogItemMessage(
                DEVICE_IDENTIFICATION,
                MessageType.FROM_DEVICE,
            ).also { logItemMessageToDevice ->
                assertNotNull(logItemMessageToDevice)
                assertEquals(MessageType.FROM_DEVICE, logItemMessageToDevice.messageType)
                assertEquals("", logItemMessageToDevice.organizationIdentification)
                assertEquals(45, logItemMessageToDevice.rawDataSize)
                assertEquals(ByteString.fromHex(expectedReceivedRawMessage), logItemMessageToDevice.rawData)
            }

        messageBroker
            .receiveLogItemMessage(
                DEVICE_IDENTIFICATION,
                MessageType.TO_DEVICE,
            ).also { logItemReplyFromDevice ->
                assertNotNull(logItemReplyFromDevice)
                assertEquals(MessageType.TO_DEVICE, logItemReplyFromDevice.messageType)
                assertEquals("", logItemReplyFromDevice.organizationIdentification)
                assertEquals(5, logItemReplyFromDevice.rawDataSize)
                assertEquals(ByteString.fromHex("9201020800"), logItemReplyFromDevice.rawData)
            }
    }

    val okMock =
        DeviceSimulator.DeviceCallMock {
            message {
                getConfigurationResponse =
                    getConfigurationResponse {
                        status = Oslp.Status.OK
                        preferredLinkType = Oslp.LinkType.CDMA
                    }
            }
        }

    val rejectedMock =
        DeviceSimulator.DeviceCallMock {
            message {
                getConfigurationResponse =
                    getConfigurationResponse {
                        status = Oslp.Status.REJECTED
                    }
            }
        }
}
