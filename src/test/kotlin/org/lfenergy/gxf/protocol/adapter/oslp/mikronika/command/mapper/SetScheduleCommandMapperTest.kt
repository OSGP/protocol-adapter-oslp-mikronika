// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetScheduleRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.scheduleEntry
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setScheduleRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setScheduleResponse
import kotlin.test.assertEquals

class SetScheduleCommandMapperTest {
    private val subject: SetScheduleCommandMapper = SetScheduleCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
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

        val result = subject.toInternal(deviceRequestMessage) as SetScheduleRequest

        assertEquals(DEVICE_IDENTIFICATION, result.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.networkAddress)
        assertEquals(2, result.scheduleEntries.size)

        val pageInfo = result.pageInfo

        assertEquals(1, pageInfo.totalPages)
        assertEquals(result.scheduleEntries.size, pageInfo.pageSize)
        assertEquals(1, pageInfo.currentPage)
    }

    @Test
    fun `should map toResponse correctly`() {
        val envelope = mockk<Envelope>()

        val message =
            message {
                setScheduleResponse =
                    setScheduleResponse {
                        status = Oslp.Status.OK
                    }
            }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(Result.OK, result.result)
    }
}
