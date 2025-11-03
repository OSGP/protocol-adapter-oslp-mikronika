// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory.Companion.EVENT_NOTIFICATION_STRATEGY
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceNotificationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceNotificationType

import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component(EVENT_NOTIFICATION_STRATEGY)
class EventNotificationRequestStrategy(
    signingService: SigningService,
    mikronikaDeviceService: MikronikaDeviceService,
    private val eventPublisher: ApplicationEventPublisher,
) : ReceiveStrategy(signingService, mikronikaDeviceService) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    override fun handle(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ) {
        mikronikaDevice.sequenceNumber = requestEnvelope.sequenceNumber
        publishEvent(requestEnvelope, mikronikaDevice)
    }

    override fun buildResponsePayload(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ): Message =
        Message
            .newBuilder()
            .setEventNotificationResponse(Oslp.EventNotificationResponse.newBuilder().setStatus(Oslp.Status.OK))
            .build()

    private fun publishEvent(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ) {
        with(requestEnvelope.message.eventNotificationRequest) {
            for (eventNotification in notificationsList) {
                eventPublisher.publishEvent(
                    eventNotification.toDeviceNotificationReceivedEvent(mikronikaDevice),
                )
            }
        }
    }

    private fun Oslp.EventNotification.toDeviceNotificationReceivedEvent(device: MikronikaDevice) =
        DeviceNotificationReceivedEvent(
            deviceIdentification = device.deviceIdentification,
            description = this.description,
            index = this.index.let { if (it.isEmpty) 0 else it.byteAt(0).toInt() },
            dateTime = this.timestamp.toZonedDateTime(),
            eventType = DeviceNotificationType.valueOf(this.event.name),
            deviceUid = device.deviceUid,
        )

    private fun String.toZonedDateTime(): ZonedDateTime =
        LocalDateTime
            .parse(this, dateTimeFormatter)
            .atZone(ZoneId.of("UTC"))
}
