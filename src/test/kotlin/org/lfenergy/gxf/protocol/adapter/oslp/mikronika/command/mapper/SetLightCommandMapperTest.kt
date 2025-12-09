// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetLightRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RelayIndex
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.lightValue
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setLightRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setLightResponse
import kotlin.test.assertEquals

class SetLightCommandMapperTest {
    private val subject: SetLightCommandMapper = SetLightCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
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

        val result = subject.toInternal(deviceRequestMessage) as SetLightRequest

        assertEquals(DEVICE_IDENTIFICATION, result.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.networkAddress)
        assertEquals(2, result.lightValues.size)
    }

    @Test
    fun `should map toResponse correctly`() {
        val envelope = mockk<Envelope>()

        val message =
            message {
                setLightResponse =
                    setLightResponse {
                        status = Oslp.Status.OK
                    }
            }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(Result.OK, result.result)
    }
}
