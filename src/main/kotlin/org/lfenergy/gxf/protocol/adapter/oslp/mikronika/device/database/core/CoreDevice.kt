// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "device")
data class CoreDevice(
    @Id
    var id: Long? = null,
    var deviceIdentification: String,
    @Column(name = "gps_latitude") var latitude: Double,
    @Column(name = "gps_longitude") var longitude: Double,
)
