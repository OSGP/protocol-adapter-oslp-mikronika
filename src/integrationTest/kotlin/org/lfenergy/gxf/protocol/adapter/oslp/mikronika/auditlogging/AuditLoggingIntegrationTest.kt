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
import org.lfenergy.gxf.publiclighting.contracts.internal.auditlogging.Direction
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
            ).also { logItemMessageToDevice ->
                assertNotNull(logItemMessageToDevice)
                assertEquals(Direction.TO_DEVICE, logItemMessageToDevice.direction)
                assertEquals("LianderNetManagement", logItemMessageToDevice.organizationIdentification)
                assertEquals(ByteString.fromHex("9A0200"), logItemMessageToDevice.rawData)
                assertEquals(expectedDeviceRequest, logItemMessageToDevice.decodedData)
            }

        messageBroker
            .receiveLogItemMessage(
                DEVICE_IDENTIFICATION,
            ).also { logItemReplyFromDevice ->
                assertNotNull(logItemReplyFromDevice)
                assertEquals(Direction.FROM_DEVICE, logItemReplyFromDevice.direction)
                assertEquals("LianderNetManagement", logItemReplyFromDevice.organizationIdentification)
                assertEquals(ByteString.fromHex("A2020408003002"), logItemReplyFromDevice.rawData)
                assertEquals(expectedDeviceResponse, logItemReplyFromDevice.decodedData)
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
            ).also { logItemMessageToDevice ->
                assertNotNull(logItemMessageToDevice)
                assertEquals(Direction.FROM_DEVICE, logItemMessageToDevice.direction)
                assertEquals(NO_ORGANISATION, logItemMessageToDevice.organizationIdentification)
                assertEquals(ByteString.fromHex(expectedReceivedRawMessage), logItemMessageToDevice.rawData)
                assertEquals(expectedNotificationEvent, logItemMessageToDevice.decodedData)
            }

        messageBroker
            .receiveLogItemMessage(
                DEVICE_IDENTIFICATION,
            ).also { logItemReplyFromDevice ->
                assertNotNull(logItemReplyFromDevice)
                assertEquals(Direction.TO_DEVICE, logItemReplyFromDevice.direction)
                assertEquals(NO_ORGANISATION, logItemReplyFromDevice.organizationIdentification)
                assertEquals(ByteString.fromHex("9201020800"), logItemReplyFromDevice.rawData)
                assertEquals(expectedNotificationResponse, logItemReplyFromDevice.decodedData)
            }
    }

    val expectedDeviceRequest =
        """
        getConfigurationRequest {
        }
        
        """.trimIndent()

    val expectedDeviceResponse =
        """
        getConfigurationResponse {
          status: OK
          preferredLinkType: CDMA
        }
        
        """.trimIndent()

    val expectedNotificationEvent =
        """
        eventNotificationRequest {
          notifications {
            event: DIAG_EVENTS_GENERAL
            index: "0"
            description: "Just a test event"
            timestamp: "20251117101530"
          }
        }
        
        """.trimIndent()
    val expectedNotificationResponse =
        """
        eventNotificationResponse {
          status: OK
        }
        
        """.trimIndent()

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
}

private const val NO_ORGANISATION = ""
