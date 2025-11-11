// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events

import java.util.UUID

abstract class DeviceEvent(
    val deviceIdentification: String,
) {
    val correlationUid: String = UUID.randomUUID().toString()
}
