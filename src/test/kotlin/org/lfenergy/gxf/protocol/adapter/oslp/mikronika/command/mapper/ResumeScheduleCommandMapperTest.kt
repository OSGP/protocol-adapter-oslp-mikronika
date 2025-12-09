// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import com.google.protobuf.kotlin.toByteStringUtf8
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.ResumeScheduleRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.resumeScheduleRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.resumeScheduleResponse
import kotlin.test.assertEquals

class ResumeScheduleCommandMapperTest {
    private val subject: ResumeScheduleCommandMapper = ResumeScheduleCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
                resumeScheduleRequest =
                    resumeScheduleRequest {
                        index = "index".toByteStringUtf8()
                        immediate = false
                    }
            }

        val result = subject.toInternal(deviceRequestMessage) as ResumeScheduleRequest

        assertEquals(DEVICE_IDENTIFICATION, result.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.networkAddress)
        assertEquals("index", result.index)
        assertEquals(false, result.immediate)
    }

    @Test
    fun `should map toResponse correctly`() {
        val envelope = mockk<Envelope>()

        val message =
            message {
                resumeScheduleResponse =
                    resumeScheduleResponse {
                        status = Oslp.Status.OK
                    }
            }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(Result.OK, result.result)
    }
}
