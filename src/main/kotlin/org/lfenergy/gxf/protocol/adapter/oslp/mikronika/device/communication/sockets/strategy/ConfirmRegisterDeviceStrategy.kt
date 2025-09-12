// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceStateService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component

@Component("ConfirmRegisterDeviceStrategy")
class ConfirmRegisterDeviceStrategy(
    signingService: SigningService,
    mikronikaDeviceService: MikronikaDeviceService
) : ReceiveStrategy(signingService, mikronikaDeviceService) {
    private val deviceStateService = DeviceStateService.getInstance()

    override fun handle(requestEnvelope: Envelope, mikronikaDevice: MikronikaDevice) {
        with(requestEnvelope.message.confirmRegisterDeviceRequest) {
            if (randomDevice != deviceStateService.randomDevice) {
                println("Invalid randomDevice! Expected: ${deviceStateService.randomDevice} - Got: $randomDevice")
            }
            if (randomPlatform != deviceStateService.randomPlatform) {
                println("Invalid randomPlatform! Expected: ${deviceStateService.randomPlatform} - Got: $randomPlatform")
            }
        }

        deviceStateService.confirmRegisterDevice(requestEnvelope.sequenceNumber)
    }

    override fun buildResponsePayload(requestEnvelope: Envelope): Message {
        val response =
            Message
                .newBuilder()
                .setConfirmRegisterDeviceResponse(
                    Oslp.ConfirmRegisterDeviceResponse
                        .newBuilder()
                        .setRandomDevice(deviceStateService.randomDevice)
                        .setRandomPlatform(deviceStateService.randomPlatform)
                        .setSequenceWindow(1)
                        .setStatusValue(0)
                        .build(),
                ).build()
        return response
    }
}
