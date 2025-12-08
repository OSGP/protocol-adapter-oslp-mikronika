package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.getFirmwareVersionResponse
import org.opensmartgridplatform.oslp.message
import kotlin.test.assertEquals


class GetFirmwareVersionCommandMapperTest {

    private val subject: GetFirmwareVersionCommandMapper = GetFirmwareVersionCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage = deviceRequestMessage {
            header = requestHeader
        }

        val result = subject.toInternal(deviceRequestMessage)

        assertEquals(DEVICE_IDENTIFICATION, result.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.networkAddress)
    }

    @Test
    fun `should map toResponse correctly`() {
        val envelope = mockk<Envelope>()

        val message = message {
            getFirmwareVersionResponse = getFirmwareVersionResponse {
                firmwareVersion = FIRMWARE_VERSION
            }

        }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(FIRMWARE_VERSION, result.getFirmwareVersionResponse.firmwareVersion)
        assertEquals(Result.OK, result.result)
    }
}
