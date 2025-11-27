// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.DeviceReqResMapperFactory
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.springframework.stereotype.Service

@Service
class DeviceRequestService(
    private val deviceClientService: DeviceClientService,
    private val deviceResponseSender: DeviceResponseSender,
    private val mapperFactory: DeviceReqResMapperFactory,
) {
    private val logger = KotlinLogging.logger {}

    fun handleDeviceRequestMessage(requestMessage: DeviceRequestMessage) {
        val requestHeader = requestMessage.header
        val mapper = mapperFactory.getMapperFor(requestHeader.requestType)

        val deviceRequest = mapper.toInternal(requestMessage)

        deviceClientService.sendClientMessage(deviceRequest) { responseEnvelope: Envelope ->
            val message = mapper.toResponse(requestHeader, responseEnvelope)

            deviceResponseSender.send(message)
        }
    }
}
