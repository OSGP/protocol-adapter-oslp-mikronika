// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.ServerSocketMessageProcessor.Companion.REGISTER_DEVICE_STRATEGY
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

@Component(REGISTER_DEVICE_STRATEGY)
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
        val randomPlatform = Random.nextInt(RANDOM_PLATFORM_MAX)
        mikronikaDevice.randomPlatform = randomPlatform

        val offsetMinutes =
            ZoneId
                .systemDefault()
                .rules
                .getStandardOffset(Instant.now())
                .totalSeconds / 60

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
                                .setTimeOffset(offsetMinutes),
                        ).build(),
                ).build()

        return response
    }

    private companion object {
        const val RANDOM_PLATFORM_MAX = 65536
    }
}
