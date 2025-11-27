// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.springframework.stereotype.Component

@Component
class CommandMapperFactory(
    private val mappers: HashMap<String, CommandMapper>,
) {
    fun getMapperFor(requestType: RequestType): CommandMapper =
        mappers[requestType.name]
            ?: throw IllegalArgumentException("No request-response mapper found for request type: ${requestType.name}")

    companion object {
        const val GET_STATUS_REQUEST = "GET_STATUS_REQUEST"
    }
}
