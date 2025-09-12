// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService

@ExtendWith(MockKExtension::class)
class ReceiveStrategyTest {
    @MockK
    private lateinit var signingService: SigningService

    @MockK
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

    @InjectMockKs
    private lateinit var eventNotificationRequestStrategy: EventNotificationRequestStrategy

    @Test
    fun `Invoke should throw if the device Uid does not exist`() {
        val deviceUid = "unknown-device"
        val envelope = mockk<Envelope>()
        val expectedException = EntityNotFoundException("Device with identification $deviceUid not found")

        every { envelope.deviceUid } returns deviceUid.toByteArray()
        every { mikronikaDeviceService.findByDeviceUid(deviceUid) } throws expectedException

        assertThatThrownBy { eventNotificationRequestStrategy.invoke(envelope) }
            .isEqualTo(expectedException)
            .hasMessageContaining(expectedException.message)
    }

    // todo more tests here for all the signing logic etc....
}
