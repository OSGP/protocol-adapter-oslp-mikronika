// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
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
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.startSelfTestResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@EnableAsync
@ContextConfiguration(classes = [ContainerConfiguration::class, SecurityConfiguration::class])
class StartSelfTestCommandIntegrationTest {
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
    fun `should handle successful start self test request`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.START_SELF_TEST_REQUEST)
            }

        device.addMock(okMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.START_SELF_TEST_RESPONSE,
            )

        assertNotNull(result)
        assertEquals(Result.OK, result.result)

        val receivedRequest = okMock.capturedRequest.get()

        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))
    }

    @Test
    fun `should handle failed start self test request`() {
        val input =
            deviceRequestMessage {
                header = createHeader(RequestType.START_SELF_TEST_REQUEST)
            }

        device.addMock(rejectedMock)
        messageBroker.sendDeviceRequestMessage(input)

        val result =
            messageBroker.receiveDeviceResponseMessage(
                DEVICE_IDENTIFICATION,
                ResponseType.START_SELF_TEST_RESPONSE,
            )

        assertNotNull(result)
        assertEquals(Result.NOT_OK, result.result)

        val receivedRequest = rejectedMock.capturedRequest.get()

        assertEquals(DEVICE_UID, String(receivedRequest.deviceUid))
    }

    private val okMock =
        Device.DeviceCallMock {
            message {
                startSelfTestResponse =
                    startSelfTestResponse {
                        status = Oslp.Status.OK
                    }
            }
        }

    private val rejectedMock =
        Device.DeviceCallMock {
            message {
                startSelfTestResponse =
                    startSelfTestResponse {
                        status = Oslp.Status.FAILURE
                    }
            }
        }
}
