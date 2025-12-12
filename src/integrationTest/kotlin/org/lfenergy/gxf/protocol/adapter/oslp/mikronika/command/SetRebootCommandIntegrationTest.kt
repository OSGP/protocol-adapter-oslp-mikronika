// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.awaitOrFail
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.ContainerConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.SecurityConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_UID
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.createHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.database.AdapterDatabase
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.messagebroker.MessageBroker
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setRebootResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@EnableAsync
@ContextConfiguration(classes = [ContainerConfiguration::class, SecurityConfiguration::class])
class SetRebootCommandIntegrationTest {
    @Autowired
    private lateinit var adapterDatabase: AdapterDatabase

    @Autowired
    private lateinit var messageBroker: MessageBroker

    @Autowired
    private lateinit var device: Device

    @BeforeEach
    fun setup() {
        adapterDatabase.updateDeviceKey(DEVICE_IDENTIFICATION, device.publicKey)
    }

    @Test
    fun `should handle reboot request`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.REBOOT_REQUEST)
            }

        val mockedCallsJob = device.setupMock(okMock)

        messageBroker.sendDeviceRequestMessage(input)

        val result: DeviceResponseMessage =
            messageBroker.receiveDeviceResponseMessage(DEVICE_IDENTIFICATION, ResponseType.REBOOT_RESPONSE)

        mockedCallsJob.awaitOrFail()

        assertEquals(Result.OK, result.result)

        val receivedRequest = okMock.capturedRequest.get()

        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))
    }

    val okMock =
        Device.DeviceCallMock {
            message {
                setRebootResponse =
                    setRebootResponse {
                        status = Oslp.Status.OK
                    }
            }
        }
}
