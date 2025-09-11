// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.signing.SigningService
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component

@Component
class ConfirmRegisterDeviceStrategy(signingService: SigningService): RegisterDeviceStrategy(signingService) {
    private val deviceStateService = DeviceStateService.getInstance()

    override fun matches(message: Message): Boolean = message.hasConfirmRegisterDeviceRequest()

    override fun handle(requestEnvelope: Envelope) {
        with(requestEnvelope.message.confirmRegisterDeviceRequest) {
            if (randomDevice != deviceStateService.randomDevice) {
                Logger.logReceive(
                    "Invalid randomDevice! Expected: ${deviceStateService.randomDevice} - Got: $randomDevice",
                )
            }
            if (randomPlatform != deviceStateService.randomPlatform) {
                Logger.logReceive(
                    "Invalid randomPlatform! Expected: ${deviceStateService.randomPlatform} - Got: $randomPlatform",
                )
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
