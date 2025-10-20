// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.coreDevice

class CoreDeviceTest {
    @Test
    fun `should update CoreDevice fields`() {
        val device = coreDevice()

        val expectedId = 2L
        val expectedLatitude = 1.0f
        val expectedLongitude = 2.0f
        val expectedDeviceIdentification = "TST-202"

        device.apply {
            id = expectedId
            latitude = expectedLatitude
            longitude = expectedLongitude
            deviceIdentification = expectedDeviceIdentification
        }

        assertThat(device.id).isEqualTo(expectedId)
        assertThat(device.latitude).isEqualTo(expectedLatitude)
        assertThat(device.longitude).isEqualTo(expectedLongitude)
        assertThat(device.deviceIdentification).isEqualTo(expectedDeviceIdentification)
    }
}
