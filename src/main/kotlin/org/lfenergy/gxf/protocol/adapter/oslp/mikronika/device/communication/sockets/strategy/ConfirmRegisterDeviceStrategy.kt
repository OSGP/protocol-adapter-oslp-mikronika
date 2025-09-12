// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
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

    override fun handle(requestEnvelope: Envelope, mikronikaDevice: MikronikaDevice) {
        with(requestEnvelope.message.confirmRegisterDeviceRequest) {
            if (randomDevice != mikronikaDevice.randomDevice) {
                println("Invalid randomDevice! Expected: ${mikronikaDevice.randomDevice} - Got: $randomDevice")
            }
            if (randomPlatform != mikronikaDevice.randomPlatform) {
                println("Invalid randomPlatform! Expected: ${mikronikaDevice.randomPlatform} - Got: $randomPlatform")
            }
        }

        mikronikaDevice.sequenceNumber = requestEnvelope.sequenceNumber
    }

    override fun buildResponsePayload(requestEnvelope: Envelope, mikronikaDevice: MikronikaDevice): Message {
        val response =
            Message
                .newBuilder()
                .setConfirmRegisterDeviceResponse(
                    Oslp.ConfirmRegisterDeviceResponse
                        .newBuilder()
                        .setRandomDevice(mikronikaDevice.randomDevice ?: 0)
                        .setRandomPlatform(mikronikaDevice.randomPlatform ?: 0)
                        .setSequenceWindow(1)
                        .setStatusValue(0)
                        .build(),
                ).build()
        return response
    }
}
