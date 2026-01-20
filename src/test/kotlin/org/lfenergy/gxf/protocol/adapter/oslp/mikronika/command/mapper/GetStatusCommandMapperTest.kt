// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import com.google.protobuf.ByteString
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetStatusRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayIndex
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.getStatusResponse
import org.opensmartgridplatform.oslp.lightValue
import org.opensmartgridplatform.oslp.message
import kotlin.test.assertEquals

class GetStatusCommandMapperTest {
    private val subject: GetStatusCommandMapper = GetStatusCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
            }

        val result = subject.toInternal(deviceRequestMessage) as GetStatusRequest

        assertEquals(DEVICE_IDENTIFICATION, result.device.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.device.networkAddress)
    }

    @Test
    fun `should map toResponse correctly`() {
        val envelope = mockk<Envelope>()

        val message =
            message {
                getStatusResponse =
                    getStatusResponse {
                        preferredLinktype = Oslp.LinkType.CDMA
                        actualLinktype = Oslp.LinkType.CDMA
                        lightType = Oslp.LightType.LT_NOT_SET
                        eventNotificationMask = 2
                        status = Oslp.Status.OK
                        value.addAll(
                            listOf(
                                lightValue {
                                    index = ByteString.copyFrom(byteArrayOf(1.toByte()))
                                    on = true
                                },
                                lightValue {
                                    on = true
                                },
                                lightValue {
                                    index = ByteString.copyFrom(byteArrayOf(5.toByte()))
                                    on = true
                                },
                            ),
                        )
                    }
            }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(Result.OK, result.result)
        assertTrue { result.hasGetStatusResponse() }
        assertThat(result.getStatusResponse.lightValuesList.size).isEqualTo(2)
        assertThat(result.getStatusResponse.lightValuesList[0].index).isEqualTo(RelayIndex.RELAY_ONE)
        assertThat(result.getStatusResponse.lightValuesList[1].index).isEqualTo(RelayIndex.RELAY_ALL)
    }
}
