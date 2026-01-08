// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.junit.jupiter.api.BeforeEach
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.ContainerConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.SecurityConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.database.AdapterDatabase
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.DeviceSimulator
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.messagebroker.MessageBroker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@EnableAsync
@ContextConfiguration(classes = [ContainerConfiguration::class, SecurityConfiguration::class])
abstract class CommandIntegrationTest {
    @Autowired
    protected lateinit var adapterDatabase: AdapterDatabase

    @Autowired
    protected lateinit var messageBroker: MessageBroker

    @Autowired
    protected lateinit var deviceSimulator: DeviceSimulator

    @BeforeEach
    fun setup() {
        deviceSimulator.clearMocks()
        adapterDatabase.updateDeviceKey(DEVICE_IDENTIFICATION, deviceSimulator.publicKey)
    }
}
