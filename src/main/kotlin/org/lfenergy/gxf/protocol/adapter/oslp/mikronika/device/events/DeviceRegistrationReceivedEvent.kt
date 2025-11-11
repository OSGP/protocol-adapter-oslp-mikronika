// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events

class DeviceRegistrationReceivedEvent(
    deviceIdentification: String,
    val ipAddress: String,
    val deviceType: String,
    val hasSchedule: Boolean,
) : DeviceEvent(deviceIdentification)
