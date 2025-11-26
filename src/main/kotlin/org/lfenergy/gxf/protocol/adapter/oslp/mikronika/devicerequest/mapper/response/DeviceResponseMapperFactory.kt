// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.mapper.response

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.springframework.stereotype.Component

@Component
class DeviceResponseMapperFactory(
    private val mappers: HashMap<String, DeviceResponseMapper>,
) {
    fun getResponseMapperFor(requestType: RequestType): DeviceResponseMapper =
        mappers[requestType.name]
            ?: throw IllegalArgumentException("No response mapper found for request type: ${requestType.name}")
}
