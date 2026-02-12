// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.SetLightCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.ResumeScheduleRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.springframework.stereotype.Service

@Service
class SetLightRequestService(
    deviceClientService: DeviceClientService,
    private val deviceResponseSender: DeviceResponseSender,
    private val setLightCommandMapper: SetLightCommandMapper,
) : RequestService(deviceClientService) {
    override fun handleRequestMessage(requestMessage: DeviceRequestMessage) {
        setLight(requestMessage)
    }

    private fun setLight(requestMessage: DeviceRequestMessage) {
        val requestHeader = requestMessage.header

        sendDeviceRequest(
            setLightCommandMapper.toInternal(requestMessage),
            onSuccess = { responseEnvelope ->
                resumeSchedule(requestMessage, responseEnvelope)
            },
            onFailure = { exception ->
                val message = createErrorMessage(requestHeader, exception)
                deviceResponseSender.send(message)
            },
        )
    }

    private fun resumeSchedule(
        requestMessage: DeviceRequestMessage,
        setLightResponse: Envelope,
    ) {
        val requestHeader = requestMessage.header

        val request =
            ResumeScheduleRequest(
                Device(
                    requestHeader.deviceIdentification,
                    requestHeader.networkAddress,
                ),
                Organization(requestHeader.organizationIdentification),
                index = 0, // All relays
                immediate = false,
            )

        sendDeviceRequest(
            request,
            onSuccess = {
                val response = setLightCommandMapper.toResponse(requestHeader, setLightResponse)
                deviceResponseSender.send(response)
            },
            onFailure = { exception ->
                val message = createErrorMessage(requestHeader, exception)
                deviceResponseSender.send(message)
            },
        )
    }
}
