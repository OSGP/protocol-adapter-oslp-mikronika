// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDeviceRepository
import org.springframework.stereotype.Service

@Service
class MikronikaDeviceService(
    private val mikronikaDeviceRepository: MikronikaDeviceRepository,
) {
    fun findByDeviceIdentification(deviceIdentification: String): MikronikaDevice =
        mikronikaDeviceRepository.findByDeviceIdentification(deviceIdentification)
            ?: throw EntityNotFoundException("Device with identification $deviceIdentification not found")

    fun findByDeviceUid(deviceUid: String): MikronikaDevice =
        mikronikaDeviceRepository.findByDeviceUid(deviceUid)
            ?: throw EntityNotFoundException("Device with identification $deviceUid not found")

    @Transactional
    fun saveDevice(device: MikronikaDevice) = mikronikaDeviceRepository.save(device)
}
