// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request

import jakarta.persistence.EntityNotFoundException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.util.HeaderUtil.buildResponseHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.springframework.stereotype.Service

@Service
class UpdateKeyRequestService(
    deviceClientService: DeviceClientService,
    private val mikronikaDeviceService: MikronikaDeviceService,
    private val deviceResponseSender: DeviceResponseSender,
) : RequestService(deviceClientService) {
    override fun handleRequestMessage(requestMessage: DeviceRequestMessage) {
        val deviceIdentification = requestMessage.header.deviceIdentification

        val mikronikaDevice =
            try {
                mikronikaDeviceService.findByDeviceIdentification(deviceIdentification)
            } catch (_: EntityNotFoundException) {
                MikronikaDevice(deviceIdentification = deviceIdentification)
            }

        mikronikaDevice.publicKey = requestMessage.updateKeyRequest.publicKey

        mikronikaDeviceService.saveDevice(mikronikaDevice)

        val responseMessage =
            deviceResponseMessage {
                header = buildResponseHeader(requestMessage.header)
                result = Result.OK
            }

        deviceResponseSender.send(responseMessage)
    }
}
