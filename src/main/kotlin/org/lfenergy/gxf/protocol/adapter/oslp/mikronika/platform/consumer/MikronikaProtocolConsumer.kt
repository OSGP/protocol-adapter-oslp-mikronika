// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.platform.consumer

import jakarta.jms.Message
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
class MikronikaProtocolConsumer {
    @JmsListener(destination = "mikronika-incoming-requests-queue")
    fun receive(
        request: MikronikaRequest,
        message: Message,
    ) {
        println("Received body: $request")

        val someHeader = message.getStringProperty("ip-address")
        println("Ip Address: $someHeader")
    }
}

data class MikronikaRequest(
    val status: String,
)
