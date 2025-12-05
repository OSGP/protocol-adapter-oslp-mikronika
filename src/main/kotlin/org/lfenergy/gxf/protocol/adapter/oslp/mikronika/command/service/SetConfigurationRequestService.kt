// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.GetConfigurationCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.SetConfigurationCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.GetConfigurationResponse
import org.springframework.stereotype.Service

@Service
class SetConfigurationRequestService(
    deviceClientService: DeviceClientService,
    private val deviceResponseSender: DeviceResponseSender,
    private val getConfigurationCommandMapper: GetConfigurationCommandMapper,
    private val setConfigurationCommandMapper: SetConfigurationCommandMapper,
) : RequestService(deviceClientService) {
    fun handleSetConfigurationRequest(requestMessage: DeviceRequestMessage) {
        val requestHeader = requestMessage.header
        val getConfigurationRequest = getConfigurationCommandMapper.toInternal(requestMessage)

        sendDeviceRequest(
            getConfigurationRequest,
            onSuccess = { responseEnvelope ->
                val message = getConfigurationCommandMapper.toResponse(requestHeader, responseEnvelope)
                sendSetConfigurationRequest(requestMessage, message.getConfigurationResponse)
            },
            onFailure = { exception ->
                val message = createErrorMessage(requestHeader, exception)
                deviceResponseSender.send(message)
            },
        )
    }

    private fun sendSetConfigurationRequest(
        requestMessage: DeviceRequestMessage,
        configurationResponse: GetConfigurationResponse,
    ) {
        val setConfigurationRequest = setConfigurationCommandMapper.toInternal(requestMessage, configurationResponse)
        sendDeviceRequest(
            setConfigurationRequest,
            onSuccess = { responseEnvelope ->
                val message = setConfigurationCommandMapper.toResponse(requestMessage.header, responseEnvelope)
                deviceResponseSender.send(message)
            },
            onFailure = { exception ->
                val message = createErrorMessage(requestMessage.header, exception)
                deviceResponseSender.send(message)
            },
        )
    }
}
