// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_UID
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.createHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.DeviceSimulator
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.NotificationType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setEventNotificationMaskRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setEventNotificationsResponse

class SetEventNotificationMaskCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    fun `should handle successful set event notification mask command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.SET_EVENT_NOTIFICATION_MASK_REQUEST)
                setEventNotificationMaskRequest =
                    setEventNotificationMaskRequest {
                        notificationTypes.addAll(
                            listOf(
                                NotificationType.TARIFF_EVENTS,
                                NotificationType.MONITOR_EVENTS,
                            ),
                        )
                    }
            }

        deviceSimulator.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.SET_EVENT_NOTIFICATION_MASK_RESPONSE,
            )

        val receivedRequest = okMock.capturedRequest.get()
        assertTrue(receivedRequest.message.hasSetEventNotificationsRequest())
        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))

        assertNotNull(result)
        assertEquals(Result.OK, result.result)
        assertEquals(ResponseType.SET_EVENT_NOTIFICATION_MASK_RESPONSE, result.header.responseType)
    }

    @Test
    fun `should handle failed set event notification mask command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.SET_EVENT_NOTIFICATION_MASK_REQUEST)
                setEventNotificationMaskRequest =
                    setEventNotificationMaskRequest {
                        notificationTypes.addAll(
                            listOf(
                                NotificationType.TARIFF_EVENTS,
                                NotificationType.MONITOR_EVENTS,
                            ),
                        )
                    }
            }

        deviceSimulator.addMock(rejectedMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.SET_EVENT_NOTIFICATION_MASK_RESPONSE,
            )

        val receivedRequest = rejectedMock.capturedRequest.get()
        assertTrue(receivedRequest.message.hasSetEventNotificationsRequest())
        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))

        assertNotNull(result)
        assertEquals(Result.NOT_OK, result.result)
        assertEquals(ResponseType.SET_EVENT_NOTIFICATION_MASK_RESPONSE, result.header.responseType)
    }

    val okMock =
        DeviceSimulator.DeviceCallMock {
            message {
                setEventNotificationsResponse =
                    setEventNotificationsResponse {
                        status = Oslp.Status.OK
                    }
            }
        }

    val rejectedMock =
        DeviceSimulator.DeviceCallMock {
            message {
                setEventNotificationsResponse =
                    setEventNotificationsResponse {
                        status = Oslp.Status.REJECTED
                    }
            }
        }
}
