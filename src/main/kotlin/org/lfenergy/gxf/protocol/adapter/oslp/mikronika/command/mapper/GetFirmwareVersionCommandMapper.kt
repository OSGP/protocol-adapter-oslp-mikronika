// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.GET_FIRMWARE_VERSION_REQUEST
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.util.HeaderUtil.buildResponseHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetFirmwareVersionRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.FirmwareType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.firmwareVersion
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.getFirmwareVersionResponse
import org.springframework.stereotype.Component

@Component(value = GET_FIRMWARE_VERSION_REQUEST)
class GetFirmwareVersionCommandMapper : CommandMapper {
    override fun toInternal(requestMessage: DeviceRequestMessage): DeviceRequest =
        GetFirmwareVersionRequest(
            Device(
                requestMessage.header.deviceIdentification,
                requestMessage.header.networkAddress,
            ),
            Organization(requestMessage.header.organizationIdentification),
        )

    override fun toResponse(
        requestHeader: RequestHeader,
        envelope: Envelope,
    ): DeviceResponseMessage =
        deviceResponseMessage {
            header = buildResponseHeader(requestHeader)
            getFirmwareVersionResponse = getBody(envelope)
        }

    private fun getBody(envelope: Envelope) =
        getFirmwareVersionResponse {
            firmwareVersions.addAll(
                listOf(
                    firmwareVersion {
                        firmwareType = FirmwareType.FT_NOT_SET
                        version = envelope.message.getFirmwareVersionResponse.firmwareVersion
                    },
                ),
            )
        }
}
