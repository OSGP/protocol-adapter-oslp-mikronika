// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NETWORK_ADDRESS
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.ORGANIZATION_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organisation
import org.opensmartgridplatform.oslp.Oslp

class SetTransitionRequestTest {
    @Test
    fun `should map correctly`() {
        val subject =
            SetTransitionRequest(
                Device(DEVICE_IDENTIFICATION, NETWORK_ADDRESS),
                Organisation(ORGANIZATION_IDENTIFICATION),
                SetTransitionRequest.TransitionType.DAY_NIGHT,
                "time",
            )

        val result = subject.toOslpMessage()

        assertTrue(result.hasSetTransitionRequest())
        assertEquals("time", result.setTransitionRequest.time)
        assertEquals(Oslp.TransitionType.DAY_NIGHT, result.setTransitionRequest.transitionType)
    }
}
