// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.START_SELF_TEST_REQUEST
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.util.HeaderUtil.buildResponseHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.StartSelfTestRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organisation
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.stereotype.Component
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result as InternalResult

@Component(value = START_SELF_TEST_REQUEST)
class StartSelfTestCommandMapper : CommandMapper {
    override fun toInternal(requestMessage: DeviceRequestMessage): DeviceRequest =
        StartSelfTestRequest(
            Device(
                requestMessage.header.deviceIdentification,
                requestMessage.header.networkAddress,
            ),
            Organisation(requestMessage.header.organizationIdentification),
        )

    override fun toResponse(
        requestHeader: RequestHeader,
        envelope: Envelope,
    ): DeviceResponseMessage =
        deviceResponseMessage {
            header = buildResponseHeader(requestHeader)
            result =
                when (envelope.message.startSelfTestResponse.status) {
                    Oslp.Status.OK -> InternalResult.OK
                    else -> InternalResult.NOT_OK
                }
        }
}
