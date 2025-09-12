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
import kotlin.random.Random

@Component("RegisterDeviceStrategy")
class RegisterDeviceStrategy(
    signingService: SigningService,
    mikronikaDeviceService: MikronikaDeviceService,
) : ReceiveStrategy(signingService, mikronikaDeviceService) {
    override fun handle(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ) {
        mikronikaDevice.randomDevice = requestEnvelope.message.registerDeviceRequest.randomDevice
    }

    override fun buildResponsePayload(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ): Message {
        val randomPlatform = Random.nextInt(65536)
        mikronikaDevice.randomPlatform = randomPlatform

        val response =
            Message
                .newBuilder()
                .setRegisterDeviceResponse(
                    Oslp.RegisterDeviceResponse
                        .newBuilder()
                        .setRandomDevice(requestEnvelope.message.registerDeviceRequest.randomDevice)
                        .setCurrentTime(System.currentTimeMillis().toString())
                        .setStatus(Oslp.Status.OK)
                        .setRandomPlatform(randomPlatform)
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
