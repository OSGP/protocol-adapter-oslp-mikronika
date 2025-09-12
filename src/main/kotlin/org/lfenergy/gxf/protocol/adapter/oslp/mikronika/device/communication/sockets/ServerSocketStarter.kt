package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets

import jakarta.annotation.PostConstruct
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.configuration.ServerSocketConfiguration
import org.springframework.stereotype.Component

@Component
class ServerSocketStarter(
    private val serverSocket: ServerSocket,
    private val serverSocketConfiguration: ServerSocketConfiguration
) {

    @PostConstruct
    fun start() {
        serverSocket.startListening(serverSocketConfiguration.hostName, serverSocketConfiguration.port)
    }
}
