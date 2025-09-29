// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.mikronikaDevice
import java.time.Instant

class MikronikaDeviceTest {
    @Test
    fun `should update MikronikaDevice fields`() {
        val device = mikronikaDevice()

        val expectedId = 5L
        val now = Instant.now()
        val expectedVersion = 10L
        val expectedDeviceIdentification = "expectedDeviceIdentification"
        val expectedDeviceUid = "expectedDeviceUid"
        val expectedSequenceNumber = 55
        val expectedRandomDevice = 5923
        val expectedRandomNumber = 5923
        val expectedPublicKey = "ExpcetedKey"

        device.apply {
            id = expectedId
            creationTime = now
            modificationTime = now
            version = expectedVersion
            deviceIdentification = expectedDeviceIdentification
            deviceUid = expectedDeviceUid
            sequenceNumber = expectedSequenceNumber
            randomDevice = expectedRandomDevice
            randomPlatform = expectedRandomNumber
            publicKey = expectedPublicKey
        }

        assertThat(device.id).isEqualTo(expectedId)
        assertThat(device.creationTime).isEqualTo(now)
        assertThat(device.modificationTime).isEqualTo(now)
        assertThat(device.version).isEqualTo(expectedVersion)
        assertThat(device.deviceIdentification).isEqualTo(expectedDeviceIdentification)
        assertThat(device.deviceUid).isEqualTo(expectedDeviceUid)
        assertThat(device.sequenceNumber).isEqualTo(expectedSequenceNumber)
        assertThat(device.randomDevice).isEqualTo(expectedRandomDevice)
        assertThat(device.randomPlatform).isEqualTo(expectedRandomNumber)
        assertThat(device.publicKey).isEqualTo(expectedPublicKey)
    }
}
