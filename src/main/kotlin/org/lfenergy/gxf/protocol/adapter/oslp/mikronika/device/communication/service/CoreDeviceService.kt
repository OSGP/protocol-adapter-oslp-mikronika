// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import jakarta.persistence.EntityNotFoundException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core.CoreDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core.CoreDeviceRepository
import org.springframework.stereotype.Service

@Service
class CoreDeviceService(
    private val coreDeviceRepository: CoreDeviceRepository,
) {
    fun getCoreDevice(deviceIdentification: String): CoreDevice =
        coreDeviceRepository.findByDeviceIdentification(deviceIdentification)
            ?: throw EntityNotFoundException("Core device with identification $deviceIdentification not found")
}
