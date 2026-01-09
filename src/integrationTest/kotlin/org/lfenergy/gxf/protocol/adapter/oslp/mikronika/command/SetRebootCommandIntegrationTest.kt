// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_UID
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.createHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.Device
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setRebootResponse

class SetRebootCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    fun `should handle successful reboot command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.REBOOT_REQUEST)
            }

        device.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result: DeviceResponseMessage =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.REBOOT_RESPONSE,
            )

        assertNotNull(result)
        assertEquals(Result.OK, result.result)

        val receivedRequest = okMock.capturedRequest.get()

        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))
    }

    @Test
    fun `should handle failed reboot command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.REBOOT_REQUEST)
            }

        device.addMock(rejectedMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.REBOOT_RESPONSE,
            )

        assertNotNull(result)
        assertEquals(Result.NOT_OK, result.result)

        val receivedRequest = rejectedMock.capturedRequest.get()

        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))
    }

    private val okMock =
        Device.DeviceCallMock {
            message {
                setRebootResponse =
                    setRebootResponse {
                        status = Oslp.Status.OK
                    }
            }
        }

    private val rejectedMock =
        Device.DeviceCallMock {
            message {
                setRebootResponse =
                    setRebootResponse {
                        status = Oslp.Status.REJECTED
                    }
            }
        }
}
