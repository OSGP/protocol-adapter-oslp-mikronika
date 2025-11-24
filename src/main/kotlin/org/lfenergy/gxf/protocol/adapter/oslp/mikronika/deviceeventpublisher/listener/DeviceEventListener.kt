// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceNotificationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.events.DeviceRegistrationReceivedEvent
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.domain.DeviceEventMessageMapper.toDeviceEventMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.producer.DeviceEventMessageSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["device-events.enabled"], havingValue = "true")
class DeviceEventListener(
    private val deviceEventMessageSender: DeviceEventMessageSender,
) {
    private val logger = KotlinLogging.logger {}

    @Async
    @EventListener
    fun handleDeviceEvent(event: DeviceNotificationReceivedEvent) {
        log(event)
        deviceEventMessageSender.send(event.toDeviceEventMessage())
    }

    @Async
    @EventListener
    fun handleDeviceEvent(event: DeviceRegistrationReceivedEvent) {
        log(event)
        deviceEventMessageSender.send(event.toDeviceEventMessage())
    }

    private fun log(event: DeviceEvent) = logger.info {
        "Received ${event.javaClass.name} with correlation uid: ${event.correlationUid} for device: ${event.deviceIdentification}"
    }
}
