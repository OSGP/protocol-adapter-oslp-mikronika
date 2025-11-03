// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.coreDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.mikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.CoreDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceRegistrationReceivedEvent

import org.opensmartgridplatform.oslp.Oslp
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockKExtension::class)
class RegisterDeviceStrategyTest {
    @MockK
    private lateinit var signingService: SigningService

    @MockK
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

    @MockK
    private lateinit var coreDeviceService: CoreDeviceService

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var registerDeviceStrategy: RegisterDeviceStrategy

    @Test
    fun `handle should set the register device request random device on the mikronika device`() {
        val randomDevice = 1234

        val envelopeMock = mockEnvelope(randomDevice)

        val mikronikaDevice = mikronikaDevice()

        every { eventPublisher.publishEvent(any<DeviceRegistrationReceivedEvent>()) } just runs

        registerDeviceStrategy.handle(envelopeMock, mikronikaDevice)

        assertThat(mikronikaDevice.randomDevice).isEqualTo(randomDevice)

        verify {
            eventPublisher.publishEvent(
                withArg { it: DeviceRegistrationReceivedEvent ->
                    it.deviceIdentification == mikronikaDevice.deviceIdentification
                },
            )
        }
    }

    @Test
    fun `build response payload should return the correct register device response and set the random platform number`() {
        val randomDevice = 5678

        val envelopeMock = mockEnvelope(randomDevice)
        val mikronikaDevice = mockk<MikronikaDevice>(relaxed = true)
        val slot = slot<Int>()

        every { mikronikaDevice.deviceIdentification } returns DEVICE_IDENTIFICATION
        every { coreDeviceService.getCoreDevice(any()) } returns coreDevice()

        val actualPayload = registerDeviceStrategy.buildResponsePayload(envelopeMock, mikronikaDevice)

        verify { mikronikaDevice.randomPlatform = capture(slot) }
        verify { coreDeviceService.getCoreDevice(DEVICE_IDENTIFICATION) }

        assertThat(actualPayload.hasRegisterDeviceResponse()).isTrue()

        val registerDeviceResponse = actualPayload.registerDeviceResponse
        assertThat(registerDeviceResponse.randomDevice).isEqualTo(randomDevice)
        assertThat(registerDeviceResponse.status).isEqualTo(Oslp.Status.OK)
        assertThat(registerDeviceResponse.randomPlatform).isEqualTo(slot.captured)

        val locationInfo = registerDeviceResponse.locationInfo
        assertThat(locationInfo.latitude).isEqualTo(50000000)
        assertThat(locationInfo.longitude).isEqualTo(51000000)
        assertThat(locationInfo.timeOffset).isNotNull()
    }

    private fun mockEnvelope(randomDevice: Int): Envelope {
        val envelope = mockk<Envelope>(relaxed = true)
        val message = mockk<Oslp.Message>(relaxed = true)
        val registerDeviceRequest = mockk<Oslp.RegisterDeviceRequest>(relaxed = true)

        every { registerDeviceRequest.randomDevice } returns randomDevice
        every { message.registerDeviceRequest } returns registerDeviceRequest
        every { envelope.message } returns message

        return envelope
    }
}
