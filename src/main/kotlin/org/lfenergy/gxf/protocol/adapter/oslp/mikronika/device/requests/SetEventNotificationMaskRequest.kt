// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organisation
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setEventNotificationsRequest

class SetEventNotificationMaskRequest(
    device: Device,
    organisation: Organisation,
    val notificationMask: Int,
) : DeviceRequest(
        device,
        organisation,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setEventNotificationsRequest =
                setEventNotificationsRequest {
                    notificationMask = this@SetEventNotificationMaskRequest.notificationMask
                }
        }
}
