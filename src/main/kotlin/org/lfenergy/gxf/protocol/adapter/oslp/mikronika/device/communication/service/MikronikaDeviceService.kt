package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDeviceRepository
import org.springframework.stereotype.Service

@Service
class MikronikaDeviceService(private val mikronikaDeviceRepository: MikronikaDeviceRepository) {

    fun findByDeviceUid(deviceUid: String): MikronikaDevice =
        mikronikaDeviceRepository.findByDeviceUid(deviceUid)
            ?: throw EntityNotFoundException("Device with identification $deviceUid not found")

    @Transactional
    fun saveDevice(device: MikronikaDevice) =
        mikronikaDeviceRepository.save(device)

}
