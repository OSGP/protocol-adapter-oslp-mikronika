// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.NETWORK_ADDRESS

class StartSelfTestRequestTest {
    @Test
    fun `should map correctly`() {
        val subject =
            StartSelfTestRequest(
                DEVICE_IDENTIFICATION,
                NETWORK_ADDRESS,
            )

        val result = subject.toOslpMessage()

        assertTrue(result.hasStartSelfTestRequest())
    }
}
