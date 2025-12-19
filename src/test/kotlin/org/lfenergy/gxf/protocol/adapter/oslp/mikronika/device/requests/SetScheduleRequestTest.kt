// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NETWORK_ADDRESS

class SetScheduleRequestTest {
    @Test
    fun `should map correctly`() {
        val subject =
            SetScheduleRequest(
                DEVICE_IDENTIFICATION,
                NETWORK_ADDRESS,
                emptyList(),
                SetScheduleRequest.PageInfo(1, 2, 3),
            )

        val result = subject.toOslpMessage()

        assertTrue(result.hasSetScheduleRequest())
    }
}
