// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organisation
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.stopSelfTestRequest

class StopSelfTestRequest(
    device: Device,
    organisation: Organisation,
) : DeviceRequest(
        device,
        organisation,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            stopSelfTestRequest = stopSelfTestRequest { }
        }
}
