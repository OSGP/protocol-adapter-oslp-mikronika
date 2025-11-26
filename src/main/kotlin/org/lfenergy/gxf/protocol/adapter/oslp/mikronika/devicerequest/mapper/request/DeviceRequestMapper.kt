// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.mapper.request

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetStatusRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage

object DeviceRequestMapper {
    fun mapGetStatusRequest(requestMessage: DeviceRequestMessage): DeviceRequest {
        val deviceIdentification = requestMessage.header.deviceIdentification
        val networkAddress = requestMessage.header.networkAddress

        return GetStatusRequest(
            deviceIdentification,
            networkAddress,
        )
    }
}
