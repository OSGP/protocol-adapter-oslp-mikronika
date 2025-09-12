// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MikronikaRepository : JpaRepository<MikronikaDevice, Long> {
    fun findByDeviceIdentification(deviceIdentification: String): MikronikaDevice?
}
