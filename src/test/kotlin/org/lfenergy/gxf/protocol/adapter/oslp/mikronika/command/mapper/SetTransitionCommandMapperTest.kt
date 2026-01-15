// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetTransitionRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.TransitionType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setTransitionRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setTransitionResponse
import kotlin.test.assertEquals

class SetTransitionCommandMapperTest {
    private val subject: SetTransitionCommandMapper = SetTransitionCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
                setTransitionRequest =
                    setTransitionRequest {
                        transitionType = TransitionType.SUNSET
                        time = "time"
                    }
            }

        val result = subject.toInternal(deviceRequestMessage) as SetTransitionRequest

        assertEquals(DEVICE_IDENTIFICATION, result.device.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.device.networkAddress)
        assertEquals(SetTransitionRequest.TransitionType.DAY_NIGHT, result.transitionType)
        assertEquals("time", result.time)
    }

    @Test
    fun `should map toResponse correctly`() {
        val envelope = mockk<Envelope>()

        val message =
            message {
                setTransitionResponse =
                    setTransitionResponse {
                        status = Oslp.Status.OK
                    }
            }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(Result.OK, result.result)
    }
}
