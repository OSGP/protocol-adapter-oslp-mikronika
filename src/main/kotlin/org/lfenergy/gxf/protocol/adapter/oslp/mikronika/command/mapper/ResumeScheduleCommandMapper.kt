// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.RESUME_SCHEDULE_REQUEST
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.ResumeScheduleRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.stereotype.Component
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result as InternalResult

@Component(value = RESUME_SCHEDULE_REQUEST)
class ResumeScheduleCommandMapper : CommandMapper() {
    override fun toInternal(requestMessage: DeviceRequestMessage): DeviceRequest {
        val deviceIdentification = requestMessage.header.deviceIdentification
        val networkAddress = requestMessage.header.networkAddress

        return ResumeScheduleRequest(
            deviceIdentification,
            networkAddress,
            requestMessage.resumeScheduleRequest.index.number,
            requestMessage.resumeScheduleRequest.immediate,
        )
    }

    override fun toResponse(
        requestHeader: RequestHeader,
        envelope: Envelope,
    ): DeviceResponseMessage =
        deviceResponseMessage {
            header = buildResponseHeader(requestHeader)
            result =
                when (envelope.message.resumeScheduleResponse.status) {
                    Oslp.Status.OK -> InternalResult.OK
                    else -> InternalResult.NOT_OK
                }
        }
}
