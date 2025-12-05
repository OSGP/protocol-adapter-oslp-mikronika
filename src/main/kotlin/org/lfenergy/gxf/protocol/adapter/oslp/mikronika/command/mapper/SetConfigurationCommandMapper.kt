// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.SET_CONFIGURATION_REQUEST
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.GetConfigurationResponse
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.springframework.stereotype.Component

@Component(value = SET_CONFIGURATION_REQUEST)
class SetConfigurationCommandMapper : CommandMapper() {
    fun toInternal(
        requestMessage: DeviceRequestMessage,
        getConfigurationResponse: GetConfigurationResponse,
    ): DeviceRequest {
        val deviceIdentification = requestMessage.header.deviceIdentification
        val networkAddress = requestMessage.header.networkAddress

        return SetConfigurationRequest(
            deviceIdentification,
            networkAddress,
            getConfigurationResponse,
        )
    }

    override fun toInternal(requestMessage: DeviceRequestMessage): DeviceRequest =
        throw NotImplementedError("Setting the configuration requires values from the getConfiguration command.")

    override fun toResponse(
        requestHeader: RequestHeader,
        envelope: Envelope,
    ): DeviceResponseMessage =
        deviceResponseMessage {
            header = buildResponseHeader(requestHeader)
            result = Result.OK
        }
}
