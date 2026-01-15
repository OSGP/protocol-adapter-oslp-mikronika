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
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.NETWORK_ADDRESS
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.RANDOM_DEVICE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.database.AdapterDatabase
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.database.CoreDatabase
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.DeviceSimulator
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core.CoreDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.messagebroker.MessageBroker
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.ContextConfiguration

@SpringBootTest()
@EnableAsync
@ContextConfiguration(classes = [ContainerConfiguration::class, SecurityConfiguration::class])
class DeviceRegistrationIntegrationTest {
    @Autowired
    private lateinit var adapterDatabase: AdapterDatabase

    @Autowired
    private lateinit var coreDatabase: CoreDatabase

    @Autowired
    private lateinit var messageBroker: MessageBroker

    @Autowired
    private lateinit var deviceSimulator: DeviceSimulator

    private var mikronikaDevice: MikronikaDevice? = null
    private var coreDevice: CoreDevice? = null

    private var responseEnvelope: Envelope? = null

    @BeforeEach
    fun setup() {
        adapterDatabase.updateDeviceKey(DEVICE_IDENTIFICATION, deviceSimulator.publicKey)
    }

    @Test
    fun `test receiving a device registration request message`() {
        `given an existing device`()
        `when the device sends a device registration request message`()
        `then a device registration event message should be sent to the message broker`()
        `then a device registration response message should be sent back to the device`()
        `then the device should be updated in the database`()
    }

    private fun `given an existing device`() {
        mikronikaDevice = adapterDatabase.getAdapterDevice(DEVICE_IDENTIFICATION)
        coreDevice = coreDatabase.getCoreDevice(DEVICE_IDENTIFICATION)
    }

    private fun `when the device sends a device registration request message`() {
        responseEnvelope = deviceSimulator.sendDeviceRegistrationRequest()
    }

    private fun `then a device registration response message should be sent back to the device`() {
        assertThat(responseEnvelope?.message).isNotNull
        assertThat(responseEnvelope!!.message.hasRegisterDeviceResponse()).isTrue

        with(responseEnvelope!!.message.registerDeviceResponse) {
            assertThat(this).isNotNull()
            assertThat(status).isEqualTo(Oslp.Status.OK)
            assertThat(randomDevice).isEqualTo(RANDOM_DEVICE)
            assertThat(randomPlatform).isNotEqualTo(0)
        }

        with(responseEnvelope!!.message.registerDeviceResponse.locationInfo) {
            assertThat(this).isNotNull()
            assertThat(latitude).isEqualTo(coreDevice!!.latitude.scaleToGpsInt())
            assertThat(longitude).isEqualTo(coreDevice!!.longitude.scaleToGpsInt())
        }
    }

    private fun `then a device registration event message should be sent to the message broker`() {
        val eventMessage = messageBroker.receiveDeviceEventMessage(DEVICE_IDENTIFICATION, EventType.DEVICE_REGISTRATION)

        with(eventMessage.deviceRegistrationReceivedEvent) {
            assertThat(this).isNotNull
            assertThat(networkAddress).isEqualTo(NETWORK_ADDRESS)
            assertThat(hasSchedule).isTrue
        }
    }

    private fun `then the device should be updated in the database`() {
        val updatedDevice = adapterDatabase.getAdapterDevice(DEVICE_IDENTIFICATION)!!
        assertThat(updatedDevice.randomPlatform).isNotEqualTo(0)
        assertThat(updatedDevice.randomDevice).isEqualTo(RANDOM_DEVICE)
    }

    companion object {
        private const val GPS_SCALE_FACTOR = 1E6F

        private fun Float.scaleToGpsInt(): Int = (this * GPS_SCALE_FACTOR).toInt()
    }
}
