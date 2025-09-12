// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceStateService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class RegisterDeviceStrategy(
    signingService: SigningService,
) : ReceiveStrategy(signingService) {
    override fun matches(message: Message): Boolean = message.hasRegisterDeviceRequest()

    override fun handle(requestEnvelope: Envelope) {
        val deviceStateService = DeviceStateService.getInstance()
        deviceStateService.registerDevice(requestEnvelope.deviceId)
        deviceStateService.randomDevice = requestEnvelope.message.registerDeviceRequest.randomDevice
    }

    override fun buildResponsePayload(requestEnvelope: Envelope): Message {
        val deviceStateService = DeviceStateService.getInstance()

        deviceStateService.deviceId = requestEnvelope.deviceId
        deviceStateService.randomPlatform = Random.nextInt(65536)

        val response =
            Message
                .newBuilder()
                .setRegisterDeviceResponse(
                    Oslp.RegisterDeviceResponse
                        .newBuilder()
                        .setRandomDevice(requestEnvelope.message.registerDeviceRequest.randomDevice)
                        .setCurrentTime(System.currentTimeMillis().toString())
                        .setStatus(Oslp.Status.OK)
                        .setRandomPlatform(deviceStateService.randomPlatform)
                        .setLocationInfo(
                            Oslp.LocationInfo
                                .newBuilder()
                                .setLatitude(1111)
                                .setLongitude(222222)
                                .setTimeOffset(60),
                        ).build(),
                ).build()

        return response
    }
}
