// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.config

import jakarta.jms.ConnectionFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.EnableAsync

@Configuration
@EnableAsync
@EnableJms
@EnableConfigurationProperties(DeviceRequestConfigurationProperties::class)
class DeviceRequestModuleConfiguration(
    private val connectionFactory: ConnectionFactory,
    private val properties: DeviceRequestConfigurationProperties,
) {
    @Bean("deviceRequestJmsListenerContainerFactory")
    fun jmsListenerContainerFactory(): DefaultJmsListenerContainerFactory {
        val factory = DefaultJmsListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setConcurrency("${properties.consumer.minConcurrency}-${properties.consumer.maxConcurrency}")
        return factory
    }

    @Bean
    fun deviceRequestJmsTemplate(): JmsTemplate {
        val template = JmsTemplate(connectionFactory)
        template.defaultDestinationName = properties.producer.outboundQueue
        with(properties.producer.qualityOfService) {
            if (explicitQosEnabled) {
                template.isExplicitQosEnabled = explicitQosEnabled
                template.setDeliveryPersistent(deliveryPersistent)
                template.priority = priority
                template.timeToLive = timeToLive
            }
        }
        return template
    }
}
