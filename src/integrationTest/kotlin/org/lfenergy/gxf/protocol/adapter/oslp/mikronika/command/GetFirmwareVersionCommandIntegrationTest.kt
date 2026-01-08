// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.FIRMWARE_VERSION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_UID
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.createHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.DeviceSimulator
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.getFirmwareVersionResponse
import org.opensmartgridplatform.oslp.message

class GetFirmwareVersionCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    fun `should handle successful get firmware version command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.GET_FIRMWARE_VERSION_REQUEST)
            }

        deviceSimulator.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result: DeviceResponseMessage =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.GET_FIRMWARE_VERSION_RESPONSE,
            )

        val receivedRequest = okMock.capturedRequest.get()
        assertTrue(receivedRequest.message.hasGetFirmwareVersionRequest())
        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))

        assertNotNull(result)
        assertEquals(Result.OK, result.result)
        assertEquals(ResponseType.GET_FIRMWARE_VERSION_RESPONSE, result.header.responseType)
        assertEquals(FIRMWARE_VERSION, result.getFirmwareVersionResponse.getFirmwareVersions(0).version)
    }

    private val okMock =
        DeviceSimulator.DeviceCallMock {
            message {
                getFirmwareVersionResponse =
                    getFirmwareVersionResponse {
                        firmwareVersion = FIRMWARE_VERSION
                    }
            }
        }
}
