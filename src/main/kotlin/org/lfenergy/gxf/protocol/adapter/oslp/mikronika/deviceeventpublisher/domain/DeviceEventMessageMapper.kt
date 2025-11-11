// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.domain

import com.google.protobuf.Timestamp
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.DEVICE_TYPE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceNotificationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceRegistrationReceivedEvent
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceEventMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.Header
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.NotificationType
import java.time.ZonedDateTime
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceNotificationReceivedEvent as ProtobufDeviceNotificationReceivedEvent
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceRegistrationReceivedEvent as ProtobufDeviceRegistrationReceivedEvent

object DeviceEventMessageMapper {
    fun DeviceNotificationReceivedEvent.toDeviceEventMessage(): DeviceEventMessage =
        DeviceEventMessage
            .newBuilder()
            .setHeader(
                Header
                    .newBuilder()
                    .setCorrelationUid(this.correlationUid)
                    .setDeviceIdentification(this.deviceIdentification)
                    .setDeviceType(DEVICE_TYPE)
                    .setEventType(EventType.DEVICE_NOTIFICATION),
            ).setDeviceNotificationReceivedEvent(
                ProtobufDeviceNotificationReceivedEvent
                    .newBuilder()
                    .apply { this@toDeviceEventMessage.description?.let { description -> setDescription(description) } }
                    .setNotificationType(NotificationType.valueOf(this.eventType.name))
                    .setTimestamp(dateTime.toProtobufTimestamp())
                    .apply { this@toDeviceEventMessage.index?.let { index -> setIndex(index) } },
            ).build()

    fun DeviceRegistrationReceivedEvent.toDeviceEventMessage(): DeviceEventMessage =
        DeviceEventMessage
            .newBuilder()
            .setHeader(
                Header
                    .newBuilder()
                    .setCorrelationUid(this.correlationUid)
                    .setDeviceIdentification(this.deviceIdentification)
                    .setEventType(EventType.DEVICE_REGISTRATION)
                    .setDeviceType(DEVICE_TYPE),
            ).setDeviceRegistrationReceivedEvent(
                ProtobufDeviceRegistrationReceivedEvent
                    .newBuilder()
                    .setNetworkAddress(this.ipAddress)
                    .setHasSchedule(this.hasSchedule),
            ).build()

    fun ZonedDateTime.toProtobufTimestamp(): Timestamp =
        Timestamp
            .newBuilder()
            .setSeconds(this.toEpochSecond())
            .setNanos(this.nano)
            .build()
}
