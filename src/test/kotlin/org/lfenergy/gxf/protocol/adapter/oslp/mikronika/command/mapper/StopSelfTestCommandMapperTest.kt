package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import com.google.protobuf.kotlin.toByteStringUtf8
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.stopSelfTestResponse
import kotlin.test.assertEquals

class StopSelfTestCommandMapperTest {

    private val subject: StopSelfTestCommandMapper = StopSelfTestCommandMapper()

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
            stopSelfTestResponse = stopSelfTestResponse {
                status = Oslp.Status.OK
                selfTestResult = "".toByteStringUtf8()
            }
        }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(Result.OK, result.result)
        assertEquals("", result.errorResponse.errorMessage) // Empty string if result is OK
    }

}
