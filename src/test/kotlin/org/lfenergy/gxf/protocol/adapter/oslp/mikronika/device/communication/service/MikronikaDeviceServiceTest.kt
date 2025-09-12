package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDeviceRepository

@ExtendWith(MockKExtension::class)
class MikronikaDeviceServiceTest {
    @MockK
    private lateinit var mikronikaDeviceRepository: MikronikaDeviceRepository;

    @InjectMockKs
    private lateinit var mikronikaDeviceService: MikronikaDeviceService

    @Test
    fun `findDeviceByUid should return a mikronika device`() {
        val deviceUid = "my-device-uid"
        val expected = mockk<MikronikaDevice>(relaxed = true)

        every { mikronikaDeviceRepository.findByDeviceUid(deviceUid) } returns expected

        val actual = mikronikaDeviceService.findByDeviceUid(deviceUid)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `findByDeviceUid should throw if the device is not found`() {
        val deviceUid = "unknown-device"
        every { mikronikaDeviceRepository.findByDeviceUid(deviceUid) } returns null

        assertThatThrownBy { mikronikaDeviceService.findByDeviceUid(deviceUid) }
            .isInstanceOf(EntityNotFoundException::class.java)
            .hasMessageContaining("Device with identification $deviceUid not found")
    }

    @Test
    fun `saveDevice should call the save`() {
        val device = mockk<MikronikaDevice>(relaxed = true)

        every { mikronikaDeviceRepository.save(device) } returns device

        val actual = mikronikaDeviceService.saveDevice(device)

        verify(exactly = 1) { mikronikaDeviceService.saveDevice(device) }

        assertThat(actual).isEqualTo(device)
    }
}
