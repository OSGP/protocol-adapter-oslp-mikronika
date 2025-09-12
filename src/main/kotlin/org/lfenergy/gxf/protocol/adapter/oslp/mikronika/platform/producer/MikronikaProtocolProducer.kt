package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.platform.producer

import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class MikronikaProtocolProducer(
    private val jmsTemplate: JmsTemplate
) {
    fun send(response: String) {
        jmsTemplate.convertAndSend("mikronika-outgoing-responses-queue", response)
    }
}
