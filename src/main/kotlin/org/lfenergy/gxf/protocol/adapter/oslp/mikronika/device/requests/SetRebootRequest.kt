// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setRebootRequest

class SetRebootRequest(
    deviceIdentification: String,
    networkAddress: String,
    val present: Boolean,
) : DeviceRequest(
    deviceIdentification,
    networkAddress,
) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setRebootRequest {
                present = this@SetRebootRequest.present
            }
        }
}
