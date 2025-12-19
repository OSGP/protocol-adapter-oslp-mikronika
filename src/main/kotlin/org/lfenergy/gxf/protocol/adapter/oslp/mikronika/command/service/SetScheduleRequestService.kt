// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.SetScheduleCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.astronomicalOffsetsConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.configuration
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setConfigurationRequest
import org.springframework.stereotype.Service

@Service
class SetScheduleRequestService(
    deviceClientService: DeviceClientService,
    private val deviceResponseSender: DeviceResponseSender,
    private val setScheduleCommandMapper: SetScheduleCommandMapper,
) : RequestService(deviceClientService) {
    fun handleSetScheduleRequest(requestMessage: DeviceRequestMessage) {
        if (requestMessage.setScheduleRequest.hasAstronomicalSunsetOffset() ||
            requestMessage.setScheduleRequest.hasAstronomicalSunriseOffset()
        ) {
            sendSetConfigurationRequest(requestMessage)
        } else {
            sendSetScheduleRequest(requestMessage)
        }
    }

    private fun sendSetScheduleRequest(requestMessage: DeviceRequestMessage) {
        val requestHeader = requestMessage.header
        sendDeviceRequest(
            setScheduleCommandMapper.toInternal(requestMessage),
            onSuccess = { responseEnvelope ->
                val message = setScheduleCommandMapper.toResponse(requestHeader, responseEnvelope)
                deviceResponseSender.send(message)
            },
            onFailure = { exception ->
                val message = createErrorMessage(requestHeader, exception)
                deviceResponseSender.send(message)
            },
        )
    }

    private fun sendSetConfigurationRequest(requestMessage: DeviceRequestMessage) {
        sendDeviceRequest(
            createAstronomicalOffsetConfigurationRequest(requestMessage),
            onSuccess = { _ ->
                sendSetScheduleRequest(requestMessage)
            },
            onFailure = { exception ->
                val message = createErrorMessage(requestMessage.header, exception)
                deviceResponseSender.send(message)
            },
        )
    }

    private fun createAstronomicalOffsetConfigurationRequest(requestMessage: DeviceRequestMessage) =
        SetConfigurationRequest(
            requestMessage.header.deviceIdentification,
            requestMessage.header.networkAddress,
            setConfigurationRequest {
                configuration =
                    configuration {
                        astronomicalOffsetsConfiguration =
                            astronomicalOffsetsConfiguration {
                                val scheduleReq = requestMessage.setScheduleRequest
                                if (scheduleReq.hasAstronomicalSunsetOffset()) {
                                    sunsetOffset = scheduleReq.astronomicalSunsetOffset
                                }
                                if (scheduleReq.hasAstronomicalSunriseOffset()) {
                                    sunriseOffset = scheduleReq.astronomicalSunriseOffset
                                }
                            }
                    }
            },
        )
}
