package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.config

import jakarta.jms.ConnectionFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.EnableAsync

@Configuration
@EnableAsync
@EnableConfigurationProperties(DeviceEventsConfigurationProperties::class)
class DeviceEventsModuleConfiguration(
    private val connectionFactory: ConnectionFactory,
    private val properties: DeviceEventsConfigurationProperties,
) {
    @Bean
    fun jmsTemplate(): JmsTemplate {
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
