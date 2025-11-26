// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.getStatusRequest
import org.opensmartgridplatform.oslp.message

class GetStatusRequest(
    deviceIdentification: String,
    networkAddress: String,
) : DeviceRequest(
        deviceIdentification,
        networkAddress,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            getStatusRequest { }
        }
}
