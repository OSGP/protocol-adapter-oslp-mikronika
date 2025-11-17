// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.ContainerConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.SecurityConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.RANDOM_PLATFORM
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.SEQUENCE_NUMBER
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.database.AdapterDatabase
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.ContextConfiguration

@SpringBootTest()
@EnableAsync
@ContextConfiguration(classes = [ContainerConfiguration::class, SecurityConfiguration::class])
class DeviceRegistrationConfirmationIntegrationTest {
    @Autowired
    private lateinit var adapterDatabase: AdapterDatabase

    @Autowired
    private lateinit var device: Device

    private var mikronikaDevice: MikronikaDevice? = null
    private var responseEnvelope: Envelope? = null

    @BeforeEach
    fun setup() {
        adapterDatabase.updateDeviceKey(DEVICE_IDENTIFICATION, device.publicKey)
        adapterDatabase.updateRandomPlatform(DEVICE_IDENTIFICATION, RANDOM_PLATFORM)
    }

    @Test
    fun `test receiving a device registration confirmation request message`() {
        `given an existing device`()
        `when the device sends a device registration confirmation request message`()
        `then a device registration confirmation response message should be sent back to the device`()
        `then the device should be updated in the database`()
    }

    private fun `given an existing device`() {
        mikronikaDevice = adapterDatabase.getAdapterDevice(DEVICE_IDENTIFICATION)!!
    }

    private fun `when the device sends a device registration confirmation request message`() {
        responseEnvelope = device.sendDeviceRegistrationConfirmationRequest()
    }

    private fun `then a device registration confirmation response message should be sent back to the device`() {
        assertThat(responseEnvelope?.message).isNotNull
        assertThat(responseEnvelope!!.message.hasConfirmRegisterDeviceResponse()).isTrue

        with(responseEnvelope!!.message.confirmRegisterDeviceResponse) {
            assertThat(this).isNotNull()
            assertThat(status).isEqualTo(Oslp.Status.OK)
        }
    }

    private fun `then the device should be updated in the database`() {
        val updatedDevice = adapterDatabase.getAdapterDevice(DEVICE_IDENTIFICATION)!!
        assertThat(updatedDevice.sequenceNumber).isEqualTo(SEQUENCE_NUMBER)
    }
}
