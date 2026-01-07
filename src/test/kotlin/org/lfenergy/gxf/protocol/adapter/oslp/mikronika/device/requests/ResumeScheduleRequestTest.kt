// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.kotlin.toByteString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NETWORK_ADDRESS
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.ORGANIZATION_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organisation

class ResumeScheduleRequestTest {
    @Test
    fun `should map correctly`() {
        val subject =
            ResumeScheduleRequest(
                Device(DEVICE_IDENTIFICATION, NETWORK_ADDRESS),
                Organisation(ORGANIZATION_IDENTIFICATION),
                1,
                true,
            )

        val result = subject.toOslpMessage()

        assertTrue(result.hasResumeScheduleRequest())
        assertTrue(result.resumeScheduleRequest.immediate)
        assertEquals(1.toByteArray(1).toByteString(), result.resumeScheduleRequest.index)
    }
}
