// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeviceEventsConfigurationPropertiesTest {
    @Test
    fun `should create properties with default values`() {
        // Arrange
        val qos = DeviceEventsConfigurationProperties.QualityOfService()

        val producer =
            DeviceEventsConfigurationProperties.ProducerProperties(
                outboundQueue = "defaultQueue",
                qualityOfService = qos,
            )

        val props =
            DeviceEventsConfigurationProperties(
                enabled = true,
                producer = producer,
            )

        assertThat(props.enabled).isTrue
        assertThat(props.producer.outboundQueue).isEqualTo("defaultQueue")
        assertThat(props.producer.qualityOfService.priority).isEqualTo(4)
        assertThat(props.producer.qualityOfService.timeToLive).isEqualTo(0L)
        assertThat(props.producer.qualityOfService.explicitQosEnabled).isFalse()
        assertThat(props.producer.qualityOfService.deliveryPersistent).isFalse()
    }

    @Test
    fun `should set producer`() {
        // Arrange
        val producer =
            DeviceEventsConfigurationProperties.ProducerProperties(
                outboundQueue = "eventQueue",
                qualityOfService = DeviceEventsConfigurationProperties.QualityOfService(),
            )
        val newProducer =
            DeviceEventsConfigurationProperties.ProducerProperties(
                outboundQueue = "newEventQueue",
                qualityOfService = DeviceEventsConfigurationProperties.QualityOfService(),
            )
        val props =
            DeviceEventsConfigurationProperties(
                enabled = true,
                producer = producer,
            )

        // Act
        props.producer = newProducer

        // Assert
        assertThat(props.producer.outboundQueue).isEqualTo("newEventQueue")
    }

    @Test
    fun `should set quality of service`() {
        // Arrange
        val qos =
            DeviceEventsConfigurationProperties.QualityOfService(
                explicitQosEnabled = true,
                deliveryPersistent = true,
                priority = 9,
                timeToLive = 5000L,
            )

        val producer =
            DeviceEventsConfigurationProperties.ProducerProperties(
                outboundQueue = "eventQueue",
                qualityOfService = DeviceEventsConfigurationProperties.QualityOfService(),
            )

        // Act
        producer.qualityOfService = qos

        // Assert
        assertThat(producer.qualityOfService.explicitQosEnabled).isTrue
        assertThat(producer.qualityOfService.deliveryPersistent).isTrue
        assertThat(producer.qualityOfService.priority).isEqualTo(9)
        assertThat(producer.qualityOfService.timeToLive).isEqualTo(5000L)
    }

    @Test
    fun `should get and set properties correctly`() {
        val qos =
            DeviceEventsConfigurationProperties.QualityOfService(
                explicitQosEnabled = true,
                deliveryPersistent = true,
                priority = 7,
                timeToLive = 1000L,
            )
        val producer =
            DeviceEventsConfigurationProperties.ProducerProperties(
                outboundQueue = "testQueue",
                qualityOfService = qos,
            )
        val props =
            DeviceEventsConfigurationProperties(
                enabled = true,
                producer = producer,
            )

        assertThat(props.enabled).isTrue
        assertThat(props.producer.outboundQueue).isEqualTo("testQueue")
        assertThat(props.producer.qualityOfService.priority).isEqualTo(7)
        assertThat(props.producer.qualityOfService.timeToLive).isEqualTo(1000L)
        assertThat(props.producer.qualityOfService.explicitQosEnabled).isTrue
        assertThat(props.producer.qualityOfService.deliveryPersistent).isTrue

        // Change values
        props.enabled = false
        props.producer.outboundQueue = "newQueue"
        props.producer.qualityOfService.priority = 1
        props.producer.qualityOfService.timeToLive = 2000L
        props.producer.qualityOfService.explicitQosEnabled = false
        props.producer.qualityOfService.deliveryPersistent = false

        assertThat(props.enabled).isFalse
        assertThat(props.producer.outboundQueue).isEqualTo("newQueue")
        assertThat(props.producer.qualityOfService.priority).isEqualTo(1)
        assertThat(props.producer.qualityOfService.timeToLive).isEqualTo(2000L)
        assertThat(props.producer.qualityOfService.explicitQosEnabled).isFalse
        assertThat(props.producer.qualityOfService.deliveryPersistent).isFalse
    }
}
