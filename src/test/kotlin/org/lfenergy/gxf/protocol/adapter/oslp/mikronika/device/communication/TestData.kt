// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core.CoreDevice
import java.time.Instant

fun mikronikaDevice() =
    MikronikaDevice(
        id = 1L,
        creationTime = Instant.now(),
        modificationTime = Instant.now(),
        version = 1L,
        deviceIdentification = "TST-100",
        deviceUid = "01000TST-100",
        sequenceNumber = 1,
        randomDevice = 1,
        randomPlatform = 1,
        publicKey = "TEST-KEY",
    )

fun coreDevice() =
    CoreDevice(
        id = 1L,
        latitude = 50.0f,
        longitude = 51.0f,
        deviceIdentification = "TST-100",
    )
