// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.kotlin.toByteStringUtf8
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.NETWORK_ADDRESS

class ResumeScheduleRequestTest {
    @Test
    fun `should map correctly`() {
        val subject =
            ResumeScheduleRequest(
                DEVICE_IDENTIFICATION,
                NETWORK_ADDRESS,
                "index",
                true,
            )

        val result = subject.toOslpMessage()

        assertTrue(result.hasResumeScheduleRequest())
        assertTrue(result.resumeScheduleRequest.immediate)
        assertEquals("index".toByteStringUtf8(), result.resumeScheduleRequest.index)
    }
}
