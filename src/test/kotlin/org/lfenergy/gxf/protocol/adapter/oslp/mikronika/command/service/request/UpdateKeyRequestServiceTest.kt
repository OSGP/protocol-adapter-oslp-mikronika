// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceUpdateKeyRequestMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result

@ExtendWith(MockKExtension::class)
class UpdateKeyRequestServiceTest {
    @MockK
    private lateinit var deviceClientService: DeviceClientService

    @MockK
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

    @MockK
    private lateinit var deviceResponseSender: DeviceResponseSender

    @InjectMockKs
    private lateinit var updateKeyRequestService: UpdateKeyRequestService

    @Test
    fun `should create new device if it does not exist when update key request is received`() {
        every { mikronikaDeviceService.findByDeviceIdentification(any()) } throws (EntityNotFoundException())
        every { mikronikaDeviceService.saveDevice(any()) } returns mockk<MikronikaDevice>()
        every { deviceResponseSender.send(any()) } just Runs

        updateKeyRequestService.handleRequestMessage(deviceUpdateKeyRequestMessage)

        val savedDeviceSlot = slot<MikronikaDevice>()
        val deviceResponseSlot = slot<DeviceResponseMessage>()

        verify { mikronikaDeviceService.findByDeviceIdentification(DEVICE_IDENTIFICATION) }
        verify { mikronikaDeviceService.saveDevice(capture(savedDeviceSlot)) }
        verify { deviceResponseSender.send(capture(deviceResponseSlot)) }

        val savedDevice = savedDeviceSlot.captured

        assertThat(savedDevice).isNotNull
        assertThat(savedDevice.deviceIdentification).isEqualTo(DEVICE_IDENTIFICATION)
        assertThat(savedDevice.publicKey).isEqualTo("i_am_a_public_key")

        val deviceResponse = deviceResponseSlot.captured

        assertThat(deviceResponse).isNotNull
        assertThat(deviceResponse.result).isEqualTo(Result.OK)
    }

    @Test
    fun `should update device key if device already exists`() {
        every { mikronikaDeviceService.findByDeviceIdentification(any()) } returns existingDevice
        every { mikronikaDeviceService.saveDevice(any()) } returns mockk<MikronikaDevice>()
        every { deviceResponseSender.send(any()) } just Runs

        updateKeyRequestService.handleRequestMessage(deviceUpdateKeyRequestMessage)

        val savedDeviceSlot = slot<MikronikaDevice>()
        val deviceResponseSlot = slot<DeviceResponseMessage>()

        verify { mikronikaDeviceService.findByDeviceIdentification(DEVICE_IDENTIFICATION) }
        verify { mikronikaDeviceService.saveDevice(capture(savedDeviceSlot)) }
        verify { deviceResponseSender.send(capture(deviceResponseSlot)) }

        val savedDevice = savedDeviceSlot.captured

        assertThat(savedDevice).isNotNull
        assertThat(savedDevice.deviceIdentification).isEqualTo(DEVICE_IDENTIFICATION)
        assertThat(savedDevice.publicKey).isEqualTo("i_am_a_public_key")

        val deviceResponse = deviceResponseSlot.captured

        assertThat(deviceResponse).isNotNull
        assertThat(deviceResponse.result).isEqualTo(Result.OK)
    }

    @Test
    fun `should send error response when updating key fails`() {
        every { mikronikaDeviceService.findByDeviceIdentification(any()) } returns existingDevice
        every { mikronikaDeviceService.saveDevice(any()) } throws (Exception())
        every { deviceResponseSender.send(any()) } just Runs

        updateKeyRequestService.handleRequestMessage(deviceUpdateKeyRequestMessage)

        val deviceResponseSlot = slot<DeviceResponseMessage>()

        verify { mikronikaDeviceService.findByDeviceIdentification(DEVICE_IDENTIFICATION) }
        verify { mikronikaDeviceService.saveDevice(any()) }
        verify { deviceResponseSender.send(capture(deviceResponseSlot)) }

        val deviceResponse = deviceResponseSlot.captured

        assertThat(deviceResponse).isNotNull
        assertThat(deviceResponse.result).isEqualTo(Result.NOT_OK)
    }

    private val existingDevice =
        MikronikaDevice(
            deviceIdentification = DEVICE_IDENTIFICATION,
            publicKey = "this_is_saved_public_key",
        )
}
