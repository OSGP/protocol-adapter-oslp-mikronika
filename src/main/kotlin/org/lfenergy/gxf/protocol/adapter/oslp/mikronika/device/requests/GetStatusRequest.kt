// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.getStatusRequest
import org.opensmartgridplatform.oslp.message

class GetStatusRequest(
    device: Device,
    organisation: Organization,
) : DeviceRequest(
        device,
        organisation,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            getStatusRequest = getStatusRequest { }
        }
}
