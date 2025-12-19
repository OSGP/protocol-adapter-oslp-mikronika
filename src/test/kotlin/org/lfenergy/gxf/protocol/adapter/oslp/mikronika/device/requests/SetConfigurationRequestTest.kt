// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NETWORK_ADDRESS
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setConfigurationRequest

class SetConfigurationRequestTest {
    @Test
    fun `should map correctly`() {
        val subject =
            SetConfigurationRequest(
                DEVICE_IDENTIFICATION,
                NETWORK_ADDRESS,
                setConfigurationRequest = setConfigurationRequest { },
            )

        val result = subject.toOslpMessage()

        assertTrue(result.hasSetConfigurationRequest())
    }
}
