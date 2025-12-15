// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import com.google.protobuf.kotlin.toByteStringUtf8
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_UID
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.createHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.Device
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.stopSelfTestResponse

class StopSelfTestCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    fun `should handle successful stop self test request`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.STOP_SELF_TEST_REQUEST)
            }

        device.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.STOP_SELF_TEST_RESPONSE,
            )

        assertNotNull(result)
        assertEquals(Result.OK, result.result)

        val receivedRequest = okMock.capturedRequest.get()

        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))
    }

    @Test
    fun `should handle failed stop self test request`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.STOP_SELF_TEST_REQUEST)
            }

        device.addMock(rejectedMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.STOP_SELF_TEST_RESPONSE,
            )

        assertNotNull(result)
        assertEquals(Result.NOT_OK, result.result)

        val receivedRequest = rejectedMock.capturedRequest.get()

        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))
    }

    private val okMock =
        Device.DeviceCallMock {
            message {
                stopSelfTestResponse =
                    stopSelfTestResponse {
                        status = Oslp.Status.OK
                        selfTestResult = "".toByteStringUtf8() // TODO: This is mapped only if failed, but its required. -> FDP-3595
                    }
            }
        }

    private val rejectedMock =
        Device.DeviceCallMock {
            message {
                stopSelfTestResponse =
                    stopSelfTestResponse {
                        status = Oslp.Status.FAILURE
                        selfTestResult = "".toByteStringUtf8() // TODO: This is mapped only if failed, but its required. -> FDP-3595
                    }
            }
        }
}
