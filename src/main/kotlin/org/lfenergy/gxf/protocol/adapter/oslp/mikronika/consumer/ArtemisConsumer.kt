package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.consumer

import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
class ArtemisConsumer {

    @JmsListener(destination = "my-queue")
    fun receive(message: String) {
        println("Received: $message")
    }
}
