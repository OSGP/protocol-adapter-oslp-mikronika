// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.getConfigurationResponse
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setConfigurationResponse
import kotlin.test.assertEquals

class SetConfigurationCommandMapperTest {
    private val subject: SetConfigurationCommandMapper = SetConfigurationCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
            }

        val getConfigurationResponse = getConfigurationResponse { }

        val result = subject.toInternal(deviceRequestMessage, getConfigurationResponse) as SetConfigurationRequest

        assertEquals(DEVICE_IDENTIFICATION, result.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.networkAddress)
        assertEquals(getConfigurationResponse, result.getConfigurationResult)
    }

    @Test
    fun `should map toResponse correctly`() {
        val envelope = mockk<Envelope>()

        val message =
            message {
                setConfigurationResponse =
                    setConfigurationResponse {
                        status = Oslp.Status.OK
                    }
            }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(Result.OK, result.result)
    }

    @Test
    fun `should throw error when wrong toInternal overload is called`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
            }

        assertThrows(NotImplementedError::class.java) {
            subject.toInternal(deviceRequestMessage)
        }
    }
}
