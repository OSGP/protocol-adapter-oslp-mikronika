// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.DEVICE_TYPE_MIKRONIKA_OSLP
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceNotificationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceNotificationType
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceRegistrationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.domain.DeviceEventMessageMapper.toProtobufTimestamp
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceEventMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.Header
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.NotificationType
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceNotificationReceivedEvent as ProtobufDeviceNotificationReceivedEvent
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceRegistrationReceivedEvent as ProtobufDeviceRegistrationReceivedEvent

object TestObjects {
    const val DEVICE_IDENTIFICATION = "Device123"
    const val DEVICE_UID = "UID123"
    const val HAS_SCHEDULE = true
    const val INDEX = 1
    const val NETWORK_ADDRESS = "localhost"
    const val NOTIFICATION_DESCRIPTION = "Test event"
    const val NOTIFICATION_TYPE = "DIAG_EVENTS_GENERAL"
    const val OUTBOUND_QUEUE = "DeviceEventsQueue"

    val zonedDateTime: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
    val timestamp = zonedDateTime.toProtobufTimestamp()

    val deviceNotificationReceivedEvent: DeviceNotificationReceivedEvent =
        DeviceNotificationReceivedEvent(
            deviceIdentification = DEVICE_IDENTIFICATION,
            deviceUid = DEVICE_UID,
            eventType = DeviceNotificationType.valueOf(NOTIFICATION_TYPE),
            description = NOTIFICATION_DESCRIPTION,
            index = INDEX,
            dateTime = zonedDateTime,
        )
    val deviceNotificationReceivedEventWithEmptyValues: DeviceNotificationReceivedEvent =
        DeviceNotificationReceivedEvent(
            deviceIdentification = DEVICE_IDENTIFICATION,
            deviceUid = DEVICE_UID,
            eventType = DeviceNotificationType.valueOf(NOTIFICATION_TYPE),
            description = null,
            index = null,
            dateTime = zonedDateTime,
        )

    val deviceNotificationEventMessage: DeviceEventMessage =
        DeviceEventMessage
            .newBuilder()
            .setHeader(
                Header
                    .newBuilder()
                    .setCorrelationUid(deviceNotificationReceivedEvent.correlationUid)
                    .setDeviceIdentification(DEVICE_IDENTIFICATION)
                    .setEventType(EventType.DEVICE_NOTIFICATION)
                    .setDeviceType(DEVICE_TYPE_MIKRONIKA_OSLP),
            ).setDeviceNotificationReceivedEvent(
                ProtobufDeviceNotificationReceivedEvent
                    .newBuilder()
                    .setDescription(NOTIFICATION_DESCRIPTION)
                    .setNotificationType(NotificationType.valueOf(NOTIFICATION_TYPE))
                    .setTimestamp(timestamp)
                    .setIndex(INDEX),
            ).build()

    val deviceNotificationEventMessageWithEmptyValues: DeviceEventMessage =
        DeviceEventMessage
            .newBuilder()
            .setHeader(
                Header
                    .newBuilder()
                    .setCorrelationUid(deviceNotificationReceivedEventWithEmptyValues.correlationUid)
                    .setDeviceIdentification(DEVICE_IDENTIFICATION)
                    .setEventType(EventType.DEVICE_NOTIFICATION)
                    .setDeviceType(DEVICE_TYPE_MIKRONIKA_OSLP),
            ).setDeviceNotificationReceivedEvent(
                ProtobufDeviceNotificationReceivedEvent
                    .newBuilder()
                    .setDescription("")
                    .setNotificationType(NotificationType.valueOf(NOTIFICATION_TYPE))
                    .setTimestamp(timestamp),
            ).build()

    val deviceRegistrationReceivedEvent: DeviceRegistrationReceivedEvent =
        DeviceRegistrationReceivedEvent(
            deviceIdentification = DEVICE_IDENTIFICATION,
            ipAddress = NETWORK_ADDRESS,
            deviceType = DEVICE_TYPE_MIKRONIKA_OSLP,
            hasSchedule = HAS_SCHEDULE,
        )

    val deviceRegistrationEventMessage: DeviceEventMessage =
        DeviceEventMessage
            .newBuilder()
            .setHeader(
                Header
                    .newBuilder()
                    .setCorrelationUid(deviceRegistrationReceivedEvent.correlationUid)
                    .setDeviceIdentification(DEVICE_IDENTIFICATION)
                    .setEventType(EventType.DEVICE_REGISTRATION)
                    .setDeviceType(DEVICE_TYPE_MIKRONIKA_OSLP),
            ).setDeviceRegistrationReceivedEvent(
                ProtobufDeviceRegistrationReceivedEvent
                    .newBuilder()
                    .setNetworkAddress(NETWORK_ADDRESS)
                    .setHasSchedule(HAS_SCHEDULE),
            ).build()
}
