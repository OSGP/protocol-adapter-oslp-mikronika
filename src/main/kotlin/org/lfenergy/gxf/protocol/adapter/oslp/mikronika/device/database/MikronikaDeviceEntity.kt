package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class MikronikaDeviceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,

    @Column(name = "creation_time", nullable = false)
    val creationTime: LocalDateTime,

    @Column(name = "modification_time", nullable = false)
    val modificationTime: LocalDateTime,

    @Column(name = "version")
    val version: Long? = null,

    @Column(name = "device_identification", length = 40, nullable = false)
    val deviceIdentification: String,

    @Column(name = "device_type", length = 255)
    val deviceType: String?,

    @Column(name = "device_uid", length = 255)
    val deviceUid: String?,

    @Column(name = "sequence_number")
    val sequenceNumber: Int?,

    @Column(name = "random_device")
    val randomDevice: Int?,

    @Column(name = "random_platform")
    val randomPlatform: Int?,

    @Column(name = "public_key", length = 255)
    val publicKey: String?,

    @Column(name = "device_address", length = 255)
    val deviceAddress: String?,

    @Column(name = "device_port", length = 255)
    val devicePort: Int?,
)