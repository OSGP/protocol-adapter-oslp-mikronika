// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.database

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class AdapterDatabase(
    val adapterJdbcTemplate: JdbcTemplate,
) {
    fun getAdapterDevice(deviceIdentification: String): MikronikaDevice? =
        adapterJdbcTemplate.queryForObject(
            SELECT_ADAPTER_DEVICE_SQL,
            ::mapAdapterDevice,
            deviceIdentification,
        )

    fun updateDeviceKey(
        deviceIdentification: String,
        publicKey: String,
    ) {
        adapterJdbcTemplate.update(
            UPDATE_DEVICE_KEY_SQL,
            publicKey,
            deviceIdentification,
        )
    }

    fun updateRandomPlatform(
        deviceIdentification: String,
        randomPlatform: Int,
    ) {
        adapterJdbcTemplate.update(
            UPDATE_RANDOM_PLATFORM_SQL,
            randomPlatform,
            deviceIdentification,
        )
    }

    private fun mapAdapterDevice(
        rs: ResultSet,
        @Suppress("UNUSED_PARAMETER") rowNum: Int,
    ) = MikronikaDevice(
        deviceIdentification = rs.getString("device_identification"),
        randomDevice = rs.getInt("random_device"),
        randomPlatform = rs.getInt("random_platform"),
        sequenceNumber = rs.getInt("sequence_number"),
    )
}

const val SELECT_ADAPTER_DEVICE_SQL =
    "select device_identification, random_device, random_platform, sequence_number from oslp_mikronika_device where device_identification = ?"

const val UPDATE_DEVICE_KEY_SQL =
    "update oslp_mikronika_device set public_key = ? where device_identification = ?"

const val UPDATE_RANDOM_PLATFORM_SQL =
    "update oslp_mikronika_device set random_platform = ? where device_identification = ?"
