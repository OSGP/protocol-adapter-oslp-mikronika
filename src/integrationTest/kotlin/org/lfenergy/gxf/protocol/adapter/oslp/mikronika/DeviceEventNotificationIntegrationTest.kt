// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika

import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.ContainerConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.SecurityConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.EVENT_DESCRIPTION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.EVENT_TIMESTAMP
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.database.AdapterDatabase
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.messagebroker.MessageBroker
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.NotificationType
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@SpringBootTest()
@EnableAsync
@ContextConfiguration(classes = [ContainerConfiguration::class, SecurityConfiguration::class])
class DeviceEventNotificationIntegrationTest {
    @Autowired
    private lateinit var adapterDatabase: AdapterDatabase

    @Autowired
    private lateinit var messageBroker: MessageBroker

    @Autowired
    private lateinit var device: Device

    private var mikronikaDevice: MikronikaDevice? = null
    private var responseEnvelope: Envelope? = null

    @BeforeEach
    fun setup() {
        adapterDatabase.updateDeviceKey(DEVICE_IDENTIFICATION, device.publicKey)
    }

    @Test
    fun `test receiving an event notification request message`() {
        `given an existing device`()
        `when the device sends an event notification request message`()
        `then a device notification event message should be sent to the message broker`()
        `then an event notification response message should be sent back to the device`()
    }

    private fun `given an existing device`() {
        mikronikaDevice = adapterDatabase.getAdapterDevice(DEVICE_IDENTIFICATION)
    }

    private fun `when the device sends an event notification request message`() {
        responseEnvelope = device.sendEventNotificationRequest()
    }

    private fun `then an event notification response message should be sent back to the device`() {
        assertThat(responseEnvelope?.message).isNotNull
        assertThat(responseEnvelope!!.message.hasEventNotificationResponse()).isTrue

        with(responseEnvelope!!.message.eventNotificationResponse) {
            assertThat(this).isNotNull()
            assertThat(status).isEqualTo(Oslp.Status.OK)
        }
    }

    private fun `then a device notification event message should be sent to the message broker`() {
        val eventMessage = messageBroker.receiveDeviceEventMessage(DEVICE_IDENTIFICATION, EventType.DEVICE_NOTIFICATION)

        with(eventMessage.deviceNotificationReceivedEvent) {
            assertThat(this).isNotNull
            assertThat(notificationType).isEqualTo(NotificationType.DIAG_EVENTS_GENERAL)
            assertThat(description).isEqualTo(EVENT_DESCRIPTION)
            assertThat(timestamp).isEqualTo(EVENT_TIMESTAMP.parseToProtoTimestamp())
        }
    }

    private fun String.parseToProtoTimestamp(): Timestamp {
        val instant = LocalDateTime.parse(this, dateTimeFormatter).atZone(ZoneOffset.UTC).toInstant()
        return timestamp {
            seconds = instant.epochSecond
            nanos = instant.nano
        }
    }

    companion object {
        private const val DATE_TIME_FORMAT = "yyyyMMddHHmmss"
        private val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
    }
}
