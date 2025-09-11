package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MikronikaRepository : JpaRepository<MikronikaDeviceEntity, String> {

    fun findByDeviceIdentification(deviceIdentification: String): MikronikaDeviceEntity?
}