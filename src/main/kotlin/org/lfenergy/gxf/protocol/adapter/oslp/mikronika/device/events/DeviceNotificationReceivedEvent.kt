// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events

import java.time.ZonedDateTime

class DeviceNotificationReceivedEvent(
    deviceIdentification: String,
    val deviceUid: String?,
    val dateTime: ZonedDateTime,
    val eventType: DeviceNotificationType,
    val description: String?,
    val index: Int?,
) : DeviceEvent(deviceIdentification)
