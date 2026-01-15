// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.auditlogging.config

import jakarta.jms.ConnectionFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.EnableAsync

@Configuration
@EnableAsync
@EnableConfigurationProperties(AuditLoggingConfigurationProperties::class)
class AuditLoggingConfiguration(
    private val connectionFactory: ConnectionFactory,
    private val properties: AuditLoggingConfigurationProperties,
) {
    @Bean
    fun auditLoggingJmsTemplate(): JmsTemplate {
        val template = JmsTemplate(connectionFactory)
        template.defaultDestinationName = properties.producer.outboundQueue
        with(properties.producer.qualityOfService) {
            template.isExplicitQosEnabled = explicitQosEnabled
            if (explicitQosEnabled) {
                template.setDeliveryPersistent(deliveryPersistent)
                template.priority = priority
                template.timeToLive = timeToLive
            }
        }
        return template
    }
}
