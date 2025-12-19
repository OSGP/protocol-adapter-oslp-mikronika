// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.opensmartgridplatform.oslp.Oslp

abstract class DeviceRequest(
    val deviceIdentification: String,
    val networkAddress: String,
) {
    abstract fun toOslpMessage(): Oslp.Message
}
