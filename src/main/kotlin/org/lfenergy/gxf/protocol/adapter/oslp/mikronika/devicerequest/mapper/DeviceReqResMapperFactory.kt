// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.mapper

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.springframework.stereotype.Component

@Component
class DeviceReqResMapperFactory(
    private val mappers: HashMap<String, DeviceReqResMapper>,
) {
    fun getMapperFor(requestType: RequestType): DeviceReqResMapper =
        mappers[requestType.name]
            ?: throw IllegalArgumentException("No request-response mapper found for request type: ${requestType.name}")

    companion object {
        const val GET_STATUS_REQUEST = "GET_STATUS_REQUEST"
    }
}
