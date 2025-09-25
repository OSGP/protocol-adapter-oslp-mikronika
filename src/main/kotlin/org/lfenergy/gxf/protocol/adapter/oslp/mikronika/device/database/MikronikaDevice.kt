// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant

@Entity
@Table(name = "oslp_mikronika_device")
class MikronikaDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oslp_mikronika_device_id_seq_gen")
    @SequenceGenerator(
        name = "oslp_mikronika_device_id_seq_gen",
        sequenceName = "oslp_mikronika_device_id_seq",
        allocationSize = 1,
    )
    @Column(name = "id", nullable = false)
    var id: Long? = null,
    @Column(name = "creation_time", nullable = false)
    var creationTime: Instant = Instant.now(),
    @Column(name = "modification_time", nullable = false)
    var modificationTime: Instant = Instant.now(),
    @Version
    var version: Long? = null,
    @Column(name = "device_identification", length = 40, nullable = false)
    var deviceIdentification: String = "",
    @Column(name = "device_uid", length = 255)
    var deviceUid: String? = null,
    @Column(name = "sequence_number")
    var sequenceNumber: Int? = null,
    @Column(name = "random_device")
    var randomDevice: Int? = null,
    @Column(name = "random_platform")
    var randomPlatform: Int? = null,
    @Column(name = "public_key", length = 255, nullable = false)
    var publicKey: String = "",
)
