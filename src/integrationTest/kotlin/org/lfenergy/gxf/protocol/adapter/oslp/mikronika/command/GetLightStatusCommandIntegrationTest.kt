// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_UID
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.createHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.Device
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.getStatusResponse
import org.opensmartgridplatform.oslp.message

class GetLightStatusCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    fun `should handle successful get light status command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.GET_LIGHT_STATUS_REQUEST)
            }

        device.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.GET_LIGHT_STATUS_RESPONSE,
            )

        val receivedRequest = okMock.capturedRequest.get()
        assertThat(receivedRequest.message.hasGetStatusRequest()).isTrue()
        assertThat(receivedRequest.message.hasGetStatusRequest()).isTrue()
        assertThat(String(receivedRequest.deviceUid)).isEqualTo(DEVICE_UID)

        assertThat(result).isNotNull()
        assertThat(result.result).isEqualTo(Result.OK)
        assertThat(result.header.responseType).isEqualTo(ResponseType.GET_LIGHT_STATUS_RESPONSE)
    }

    @Test
    fun `should handle failed get light status command`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.GET_LIGHT_STATUS_REQUEST)
            }

        device.addMock(rejectedMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.GET_LIGHT_STATUS_RESPONSE,
            )

        val receivedRequest = rejectedMock.capturedRequest.get()
        assertThat(String(receivedRequest.deviceUid)).isEqualTo(DEVICE_UID)

        assertThat(result).isNotNull()
        assertThat(result.result).isEqualTo(Result.NOT_OK)
        assertThat(result.header.responseType).isEqualTo(ResponseType.GET_LIGHT_STATUS_RESPONSE)
    }

    val okMock =
        Device.DeviceCallMock {
            message {
                getStatusResponse =
                    getStatusResponse {
                        status = Oslp.Status.OK
                        preferredLinktype = Oslp.LinkType.CDMA
                        actualLinktype = Oslp.LinkType.ETHERNET
                        lightType = Oslp.LightType.RELAY
                        eventNotificationMask = 2
                    }
            }
        }

    val rejectedMock =
        Device.DeviceCallMock {
            message {
                getStatusResponse =
                    getStatusResponse {
                        status = Oslp.Status.REJECTED
                        preferredLinktype = Oslp.LinkType.CDMA
                        actualLinktype = Oslp.LinkType.ETHERNET
                        lightType = Oslp.LightType.RELAY
                        eventNotificationMask = 2
                    }
            }
        }
}
