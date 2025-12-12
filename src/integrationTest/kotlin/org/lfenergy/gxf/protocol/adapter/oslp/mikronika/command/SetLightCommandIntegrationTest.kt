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
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.Device
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RelayIndex
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.lightValue
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setLightRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setLightResponse

class SetLightCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    fun `should handle successful set light command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.SET_LIGHT_REQUEST)
                setLightRequest =
                    setLightRequest {
                        lightValues.addAll(
                            listOf(
                                lightValue {
                                    index = RelayIndex.RELAY_ONE
                                    lightOn = true
                                },
                                lightValue {
                                    index = RelayIndex.RELAY_TWO
                                    lightOn = false
                                },
                            ),
                        )
                    }
            }

        device.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.SET_LIGHT_RESPONSE,
            )

        val receivedRequest = okMock.capturedRequest.get()
        assertTrue(receivedRequest.message.hasSetLightRequest())
        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))

        assertNotNull(result)
        assertEquals(Result.OK, result.result)
        assertEquals(ResponseType.SET_LIGHT_RESPONSE, result.header.responseType)
    }

    @Test
    fun `should handle failed set light command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.SET_LIGHT_REQUEST)
                setLightRequest =
                    setLightRequest {
                        lightValues.addAll(
                            listOf(
                                lightValue {
                                    index = RelayIndex.RELAY_ONE
                                    lightOn = true
                                },
                                lightValue {
                                    index = RelayIndex.RELAY_TWO
                                    lightOn = false
                                },
                            ),
                        )
                    }
            }

        device.addMock(rejectedMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.SET_LIGHT_RESPONSE,
            )

        val receivedRequest = rejectedMock.capturedRequest.get()
        assertTrue(receivedRequest.message.hasSetLightRequest())
        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))

        assertNotNull(result)
        assertEquals(Result.NOT_OK, result.result)
        assertEquals(ResponseType.SET_LIGHT_RESPONSE, result.header.responseType)
    }

    val okMock =
        Device.DeviceCallMock {
            message {
                setLightResponse =
                    setLightResponse {
                        status = Oslp.Status.OK
                    }
            }
        }

    val rejectedMock =
        Device.DeviceCallMock {
            message {
                setLightResponse =
                    setLightResponse {
                        status = Oslp.Status.REJECTED
                    }
            }
        }
}
