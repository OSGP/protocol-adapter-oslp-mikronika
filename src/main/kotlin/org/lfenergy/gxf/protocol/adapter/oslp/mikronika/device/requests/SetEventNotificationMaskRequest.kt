// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setEventNotificationsRequest

class SetEventNotificationMaskRequest(
    deviceIdentification: String,
    networkAddress: String,
    val notificationMask: Int,
) : DeviceRequest(
        deviceIdentification,
        networkAddress,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setEventNotificationsRequest {
                notificationMask = this@SetEventNotificationMaskRequest.notificationMask
            }
        }
}
