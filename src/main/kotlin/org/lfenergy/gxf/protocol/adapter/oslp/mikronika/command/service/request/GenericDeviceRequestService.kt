// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.springframework.stereotype.Service

@Service
class GenericDeviceRequestService(
    deviceClientService: DeviceClientService,
    private val deviceResponseSender: DeviceResponseSender,
    private val mapperFactory: CommandMapperFactory,
) : RequestService(deviceClientService) {
    override fun handleRequestMessage(requestMessage: DeviceRequestMessage) {
        val requestHeader = requestMessage.header

        val mapper = mapperFactory.getMapperFor(requestHeader.requestType)

        val deviceRequest = mapper.toInternal(requestMessage)

        sendDeviceRequest(
            deviceRequest,
            onSuccess = { responseEnvelope ->
                val message = mapper.toResponse(requestHeader, responseEnvelope)
                deviceResponseSender.send(message)
            },
            onFailure = { exception ->
                val message = createErrorMessage(requestHeader, exception)
                deviceResponseSender.send(message)
            },
        )
    }
}
