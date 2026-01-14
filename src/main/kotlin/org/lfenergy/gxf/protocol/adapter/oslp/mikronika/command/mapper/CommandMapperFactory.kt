// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.springframework.stereotype.Component

@Component
class CommandMapperFactory(
    private val mappers: Map<String, CommandMapper>,
) {
    fun getMapperFor(requestType: RequestType): CommandMapper =
        mappers[requestType.name]
            ?: throw IllegalArgumentException("No request-response mapper found for request type: ${requestType.name}")

    companion object {
        const val GET_STATUS_REQUEST = "GET_STATUS_REQUEST"
        const val SET_TRANSITION_REQUEST = "SET_TRANSITION_REQUEST"
        const val RESUME_SCHEDULE_REQUEST = "RESUME_SCHEDULE_REQUEST"
        const val SET_LIGHT_REQUEST = "SET_LIGHT_REQUEST"
        const val GET_FIRMWARE_VERSION_REQUEST = "GET_FIRMWARE_VERSION_REQUEST"
        const val GET_CONFIGURATION_REQUEST = "GET_CONFIGURATION_REQUEST"
        const val SET_CONFIGURATION_REQUEST = "SET_CONFIGURATION_REQUEST"
        const val START_SELF_TEST_REQUEST = "START_SELF_TEST_REQUEST"
        const val STOP_SELF_TEST_REQUEST = "STOP_SELF_TEST_REQUEST"
        const val SET_EVENT_NOTIFICATION_MASK_REQUEST = "SET_EVENT_NOTIFICATION_MASK_REQUEST"
        const val REBOOT_REQUEST = "REBOOT_REQUEST"
        const val SET_SCHEDULE_REQUEST = "SET_SCHEDULE_REQUEST"
        const val GET_LIGHT_STATUS_REQUEST = "GET_LIGHT_STATUS_REQUEST"
    }
}
