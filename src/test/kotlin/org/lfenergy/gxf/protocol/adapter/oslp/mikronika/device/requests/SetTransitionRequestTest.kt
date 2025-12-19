// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NETWORK_ADDRESS
import org.opensmartgridplatform.oslp.Oslp

class SetTransitionRequestTest {
    @Test
    fun `should map correctly`() {
        val subject =
            SetTransitionRequest(
                DEVICE_IDENTIFICATION,
                NETWORK_ADDRESS,
                SetTransitionRequest.TransitionType.DAY_NIGHT,
                "time",
            )

        val result = subject.toOslpMessage()

        assertTrue(result.hasSetTransitionRequest())
        assertEquals("time", result.setTransitionRequest.time)
        assertEquals(Oslp.TransitionType.DAY_NIGHT, result.setTransitionRequest.transitionType)
    }
}
