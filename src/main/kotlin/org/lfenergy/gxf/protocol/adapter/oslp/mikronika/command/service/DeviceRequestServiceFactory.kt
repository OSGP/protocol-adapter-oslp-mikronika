// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request.GenericDeviceRequestService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request.RequestService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request.SetScheduleRequestService
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType.SET_SCHEDULE_REQUEST
import org.springframework.stereotype.Service

@Service
class DeviceRequestServiceFactory(
    private val genericDeviceRequestService: GenericDeviceRequestService,
    private val setScheduleRequestService: SetScheduleRequestService,
) {
    fun getDeviceRequestServiceFor(requestMessage: DeviceRequestMessage): RequestService =
        when (requestMessage.header.requestType) {
            SET_SCHEDULE_REQUEST -> setScheduleRequestService
            else -> genericDeviceRequestService
        }
}
