// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.mikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.opensmartgridplatform.oslp.Oslp

@ExtendWith(MockKExtension::class)
class ConfirmRegisterDeviceStrategyTest {
    @MockK
    private lateinit var signingService: SigningService

    @MockK
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

    @InjectMockKs
    private lateinit var confirmRegisterDeviceStrategy: ConfirmRegisterDeviceStrategy

    @Test
    fun `handle should throw InvalidRequestException when random device number does not match`() {
        val mikronikaDevice = mikronikaDevice()
        val unknownNumber = 999999

        val envelopeMock = mockEnvelope(unknownNumber)

        assertThatThrownBy { confirmRegisterDeviceStrategy.handle(envelopeMock, mikronikaDevice) }
            .isInstanceOf(InvalidRequestException::class.java)
            .hasMessageContaining("Invalid randomDevice! Expected: ${mikronikaDevice.randomDevice} - Got: $unknownNumber")
    }

    @Test
    fun `handle should throw InvalidRequestException when random platform number does not match`() {
        val mikronikaDevice = mikronikaDevice()
        val unknownNumber = 999999

        val envelopeMock = mockEnvelope(randomDevice = mikronikaDevice.randomDevice!!, randomPlatform = unknownNumber)

        assertThatThrownBy { confirmRegisterDeviceStrategy.handle(envelopeMock, mikronikaDevice) }
            .isInstanceOf(InvalidRequestException::class.java)
            .hasMessageContaining("Invalid randomPlatform! Expected: ${mikronikaDevice.randomPlatform} - Got: $unknownNumber")
    }

    @Test
    fun `handle should update the sequence number when random numbers match`() {
        val mikronikaDevice = mikronikaDevice()

        val envelopeMock =
            mockEnvelope(
                randomDevice = mikronikaDevice.randomDevice!!,
                randomPlatform = mikronikaDevice.randomPlatform!!,
            )

        val expectedSequenceNumber = 42
        every { envelopeMock.sequenceNumber } returns expectedSequenceNumber

        confirmRegisterDeviceStrategy.handle(envelopeMock, mikronikaDevice)

        assertThat(mikronikaDevice.sequenceNumber).isEqualTo(expectedSequenceNumber)
    }

    @Test
    fun `build response payload should return the correct confirm register device response`() {
        val envelope = mockk<Envelope>(relaxed = true)
        val mikronikaDevice = mikronikaDevice()

        val actual = confirmRegisterDeviceStrategy.buildResponsePayload(envelope, mikronikaDevice)

        assertThat(actual.hasConfirmRegisterDeviceResponse()).isTrue()
        val response = actual.confirmRegisterDeviceResponse
        assertThat(response.randomDevice).isEqualTo(mikronikaDevice.randomDevice)
        assertThat(response.randomPlatform).isEqualTo(mikronikaDevice.randomPlatform)
        assertThat(response.sequenceWindow).isEqualTo(1)
        assertThat(response.status).isEqualTo(Oslp.Status.OK)
    }

    private fun mockEnvelope(
        randomDevice: Int = 5,
        randomPlatform: Int = 12,
    ): Envelope {
        val envelope = mockk<Envelope>(relaxed = true)
        val message = mockk<Oslp.Message>(relaxed = true)
        val confirmRegisterDeviceRequest = mockk<Oslp.ConfirmRegisterDeviceRequest>(relaxed = true)

        every { confirmRegisterDeviceRequest.randomDevice } returns randomDevice
        every { confirmRegisterDeviceRequest.randomPlatform } returns randomPlatform
        every { message.confirmRegisterDeviceRequest } returns confirmRegisterDeviceRequest
        every { envelope.message } returns message

        return envelope
    }
}
