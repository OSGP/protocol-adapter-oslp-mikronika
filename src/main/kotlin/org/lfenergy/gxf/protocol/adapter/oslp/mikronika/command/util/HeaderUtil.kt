// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.util

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.responseHeader

object HeaderUtil {
    fun buildResponseHeader(req: RequestHeader): ResponseHeader =
        responseHeader {
            correlationUid = req.correlationUid
            deviceIdentification = req.deviceIdentification
            deviceType = req.deviceType
            organizationIdentification = req.organizationIdentification
            domain = req.domain
            domainVersion = req.domainVersion
            priority = req.priority
            responseType = getResponseType(req.requestType)
        }

    private fun getResponseType(requestType: RequestType): ResponseType =
        when (requestType) {
            RequestType.GET_STATUS_REQUEST -> ResponseType.GET_STATUS_RESPONSE
            RequestType.SET_LIGHT_REQUEST -> ResponseType.SET_LIGHT_RESPONSE
            RequestType.REBOOT_REQUEST -> ResponseType.REBOOT_RESPONSE
            RequestType.START_SELF_TEST_REQUEST -> ResponseType.START_SELF_TEST_RESPONSE
            RequestType.STOP_SELF_TEST_REQUEST -> ResponseType.STOP_SELF_TEST_RESPONSE
            RequestType.SET_SCHEDULE_REQUEST -> ResponseType.SET_SCHEDULE_RESPONSE
            RequestType.RESUME_SCHEDULE_REQUEST -> ResponseType.RESUME_SCHEDULE_RESPONSE
            RequestType.SET_TRANSITION_REQUEST -> ResponseType.SET_TRANSITION_RESPONSE
            RequestType.SET_EVENT_NOTIFICATION_MASK_REQUEST -> ResponseType.SET_EVENT_NOTIFICATION_MASK_RESPONSE
            RequestType.GET_CONFIGURATION_REQUEST -> ResponseType.GET_CONFIGURATION_RESPONSE
            RequestType.SET_CONFIGURATION_REQUEST -> ResponseType.SET_CONFIGURATION_RESPONSE
            RequestType.GET_FIRMWARE_VERSION_REQUEST -> ResponseType.GET_FIRMWARE_VERSION_RESPONSE
            RequestType.UNRECOGNIZED -> ResponseType.UNRECOGNIZED
        }
}
