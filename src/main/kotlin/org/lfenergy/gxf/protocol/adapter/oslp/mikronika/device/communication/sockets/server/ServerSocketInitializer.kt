// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class ServerSocketInitializer(
    private val serverSocket: ServerSocket,
    private val serverSocketConfiguration: ServerSocketConfiguration,
) {
    @PostConstruct
    fun start() {
        serverSocket.startListening(serverSocketConfiguration.hostName, serverSocketConfiguration.port)
    }
}
