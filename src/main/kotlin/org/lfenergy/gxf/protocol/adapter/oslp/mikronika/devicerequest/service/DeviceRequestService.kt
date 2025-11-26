// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.mapper.request.DeviceRequestMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.mapper.response.DeviceResponseMapperFactory
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.sender.DeviceResponseSender
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.springframework.stereotype.Service

@Service
class DeviceRequestService(
    private val deviceClientService: DeviceClientService,
    private val deviceResponseSender: DeviceResponseSender,
    private val mapperFactory: DeviceResponseMapperFactory,
) {
    private val logger = KotlinLogging.logger {}

    fun handleDeviceRequestMessage(requestMessage: DeviceRequestMessage) {
        val handler: (DeviceRequestMessage) -> DeviceRequest =
            when (requestMessage.header.requestType) {
                RequestType.GET_STATUS_REQUEST -> DeviceRequestMapper::mapGetStatusRequest
                else -> TODO()
            }

        val deviceRequest = handler.invoke(requestMessage)
        sendClientMessage(requestMessage.header, deviceRequest)
    }

    private fun sendClientMessage(
        requestHeader: RequestHeader,
        deviceRequest: DeviceRequest,
    ) {
        deviceClientService.sendClientMessage(deviceRequest) { responseEnvelope: Envelope ->
            val mapper = mapperFactory.getResponseMapperFor(requestHeader.requestType)
            val message = mapper.toResponse(requestHeader, responseEnvelope)

            deviceResponseSender.send(message)
        }
    }
}
