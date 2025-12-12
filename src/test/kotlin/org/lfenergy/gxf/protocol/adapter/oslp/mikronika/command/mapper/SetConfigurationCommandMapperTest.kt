// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.configuration
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setConfigurationResponse
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LightType as InternalLightType

class SetConfigurationCommandMapperTest {
    private val subject: SetConfigurationCommandMapper = SetConfigurationCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
                setConfigurationCommand =
                    setConfigurationRequest {
                        configuration =
                            configuration {
                                testButtonEnabled = true
                                lightType = InternalLightType.RELAY
                                timeSyncFrequency = 100
                                switchingDelay.addAll(listOf(150))
                            }
                    }
            }

        val result = subject.toInternal(deviceRequestMessage) as SetConfigurationRequest

        assertEquals(DEVICE_IDENTIFICATION, result.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.networkAddress)
        assertTrue(result.setConfigurationRequest.configuration.testButtonEnabled)
        assertEquals(InternalLightType.RELAY, result.setConfigurationRequest.configuration.lightType)
        assertEquals(100, result.setConfigurationRequest.configuration.timeSyncFrequency)
        assertEquals(1, result.setConfigurationRequest.configuration.switchingDelayCount)
        assertEquals(150, result.setConfigurationRequest.configuration.switchingDelayList[0])
    }

    @ParameterizedTest
    @MethodSource("oslpStatus")
    fun `should map toResponse correctly`(
        status: Oslp.Status,
        expectedResult: Result,
    ) {
        val envelope = mockk<Envelope>()

        val message =
            message {
                setConfigurationResponse =
                    setConfigurationResponse {
                        this.status = status
                    }
            }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)
        assertEquals(expectedResult, result.result)
    }

    companion object {
        @JvmStatic
        fun oslpStatus(): Stream<Arguments> =
            listOf(
                Arguments.of(Oslp.Status.OK, Result.OK),
                Arguments.of(Oslp.Status.FAILURE, Result.NOT_OK),
            ).stream()
    }
}
