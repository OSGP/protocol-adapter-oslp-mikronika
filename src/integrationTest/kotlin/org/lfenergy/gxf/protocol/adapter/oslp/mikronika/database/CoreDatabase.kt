// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.database

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core.CoreDevice
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class CoreDatabase(
    val coreJdbcTemplate: JdbcTemplate,
) {
    fun getCoreDevice(deviceIdentification: String): CoreDevice? =
        coreJdbcTemplate.queryForObject(
            SELECT_CORE_DEVICE_SQL,
            ::mapCoreDevice,
            deviceIdentification,
        )
}

const val SELECT_CORE_DEVICE_SQL =
    "select device_identification, gps_latitude, gps_longitude from device where device_identification = ?"

fun mapCoreDevice(
    rs: ResultSet,
    @Suppress("UNUSED_PARAMETER") rowNum: Int,
) = CoreDevice(
    deviceIdentification = rs.getString("device_identification"),
    latitude = rs.getFloat("gps_latitude"),
    longitude = rs.getFloat("gps_longitude"),
)
