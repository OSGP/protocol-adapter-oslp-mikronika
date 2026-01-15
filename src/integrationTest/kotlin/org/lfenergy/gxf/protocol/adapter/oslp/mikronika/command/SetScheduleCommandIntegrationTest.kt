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
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.scheduleEntry
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setScheduleRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setConfigurationResponse
import org.opensmartgridplatform.oslp.setScheduleResponse

class SetScheduleCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    fun `should handle successful set schedule command without astronomical values`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.SET_SCHEDULE_REQUEST)
                setScheduleRequest =
                    setScheduleRequest {
                        scheduleEntries.addAll(
                            listOf(
                                scheduleEntry { },
                                scheduleEntry { },
                            ),
                        )
                    }
            }

        deviceSimulator.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.SET_SCHEDULE_RESPONSE,
            )

        val receivedRequest = okMock.capturedRequest.get()
        assertTrue(receivedRequest.message.hasSetScheduleRequest())
        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))

        assertNotNull(result)
        assertEquals(Result.OK, result.result)
        assertEquals(ResponseType.SET_SCHEDULE_RESPONSE, result.header.responseType)
    }

    @Test
    fun `should handle failed set schedule command without astronomical values`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.SET_SCHEDULE_REQUEST)
                setScheduleRequest =
                    setScheduleRequest {
                        scheduleEntries.addAll(
                            listOf(
                                scheduleEntry { },
                                scheduleEntry { },
                            ),
                        )
                    }
            }

        deviceSimulator.addMock(rejectedMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.SET_SCHEDULE_RESPONSE,
            )

        val receivedRequest = rejectedMock.capturedRequest.get()
        assertTrue(receivedRequest.message.hasSetScheduleRequest())
        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))

        assertNotNull(result)
        assertEquals(Result.NOT_OK, result.result)
        assertEquals(ResponseType.SET_SCHEDULE_RESPONSE, result.header.responseType)
    }

    @Test
    fun `should handle successful set schedule command with astronomical values`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.SET_SCHEDULE_REQUEST)
                setScheduleRequest =
                    setScheduleRequest {
                        scheduleEntries.addAll(
                            listOf(
                                scheduleEntry { },
                                scheduleEntry { },
                            ),
                        )
                        astronomicalSunriseOffset = 1
                    }
            }

        deviceSimulator.addMock(okAstronomicalMock)
        deviceSimulator.addMock(okSetConfigurationMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.SET_SCHEDULE_RESPONSE,
            )

        val receivedSetConfigurationRequest = okSetConfigurationMock.capturedRequest.get()
        assertTrue(receivedSetConfigurationRequest.message.hasSetConfigurationRequest())
        assertEquals(DEVICE_UID, String(receivedSetConfigurationRequest.deviceUid))

        val receivedSetScheduleRequest = okAstronomicalMock.capturedRequest.get()
        assertTrue(receivedSetScheduleRequest.message.hasSetScheduleRequest())
        assertEquals(DEVICE_UID, String(receivedSetScheduleRequest.deviceUid))

        assertNotNull(result)
        assertEquals(Result.OK, result.result)
        assertEquals(ResponseType.SET_SCHEDULE_RESPONSE, result.header.responseType)
    }

    val okSetConfigurationMock =
        DeviceSimulator.DeviceCallMock {
            message {
                setConfigurationResponse =
                    setConfigurationResponse {
                        status = Oslp.Status.OK
                    }
            }
        }

    val okAstronomicalMock =
        DeviceSimulator.DeviceCallMock {
            message {
                setScheduleResponse =
                    setScheduleResponse {
                        status = Oslp.Status.OK
                    }
            }
        }

    val okMock =
        DeviceSimulator.DeviceCallMock {
            message {
                setScheduleResponse =
                    setScheduleResponse {
                        status = Oslp.Status.OK
                    }
            }
        }

    val rejectedMock =
        DeviceSimulator.DeviceCallMock {
            message {
                setScheduleResponse =
                    setScheduleResponse {
                        status = Oslp.Status.REJECTED
                    }
            }
        }
}
