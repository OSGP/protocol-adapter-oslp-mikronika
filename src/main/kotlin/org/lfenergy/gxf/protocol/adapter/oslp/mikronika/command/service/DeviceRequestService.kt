// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.errorResponse
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.responseHeader
import org.springframework.stereotype.Service

@Service
class DeviceRequestService(
    private val deviceClientService: DeviceClientService,
    private val deviceResponseSender: DeviceResponseSender,
    private val mapperFactory: CommandMapperFactory,
) {
    fun handleDeviceRequestMessage(requestMessage: DeviceRequestMessage) {
        val requestHeader = requestMessage.header
        val mapper = mapperFactory.getMapperFor(requestHeader.requestType)

        val deviceRequest = mapper.toInternal(requestMessage)

        deviceClientService.sendClientMessage(deviceRequest) { result ->
            result
                .onSuccess { responseEnvelope ->
                    val message = mapper.toResponse(requestHeader, responseEnvelope)
                    deviceResponseSender.send(message)
                }.onFailure { exception ->
                    val message = createErrorMessage(requestHeader, exception)
                    deviceResponseSender.send(message)
                }
        }
    }

    private fun createErrorMessage(
        requestHeader: RequestHeader,
        exception: Throwable,
    ): DeviceResponseMessage =
        deviceResponseMessage {
            header =
                responseHeader {
                    correlationUid = requestHeader.correlationUid
                    deviceIdentification = requestHeader.deviceIdentification
                    deviceType = requestHeader.deviceType
                    organizationIdentification = requestHeader.organizationIdentification
                    domain = requestHeader.domain
                    domainVersion = requestHeader.domainVersion
                    priority = requestHeader.priority
                }
            result = Result.NOT_OK
            errorResponse {
                errorMessage = exception.message ?: "Unknown exception"
            }
        }
}
