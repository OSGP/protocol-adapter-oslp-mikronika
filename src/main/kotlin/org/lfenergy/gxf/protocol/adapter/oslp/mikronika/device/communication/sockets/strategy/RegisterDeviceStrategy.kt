// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.DEVICE_TYPE_MIKRONIKA_OSLP
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.CoreDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory.Companion.REGISTER_DEVICE_STRATEGY
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceRegistrationReceivedEvent
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

@Component(REGISTER_DEVICE_STRATEGY)
class RegisterDeviceStrategy(
    signingService: SigningService,
    mikronikaDeviceService: MikronikaDeviceService,
    private val coreDeviceService: CoreDeviceService,
    private val eventPublisher: ApplicationEventPublisher,
) : ReceiveStrategy(signingService, mikronikaDeviceService) {
    override fun handle(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ) {
        mikronikaDevice.randomDevice = requestEnvelope.message.registerDeviceRequest.randomDevice
        publishEvent(requestEnvelope, mikronikaDevice)
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

        val coreDevice = coreDeviceService.getCoreDevice(mikronikaDevice.deviceIdentification)

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
                                .setLatitude(coreDevice.latitude.toCoordinatesInt())
                                .setLongitude(coreDevice.longitude.toCoordinatesInt())
                                .setTimeOffset(offsetMinutes),
                        ),
                ).build()

        return response
    }

    private fun publishEvent(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ) {
        with(requestEnvelope.message.registerDeviceRequest) {
            eventPublisher.publishEvent(
                DeviceRegistrationReceivedEvent(
                    mikronikaDevice.deviceIdentification,
                    ipAddress.toString(),
                    DEVICE_TYPE_MIKRONIKA_OSLP,
                    hasSchedule,
                ),
            )
        }
    }

    private fun Float.toCoordinatesInt() = (this * 1000000).toInt()

    private companion object {
        const val RANDOM_PLATFORM_MAX = 65536
    }
}
