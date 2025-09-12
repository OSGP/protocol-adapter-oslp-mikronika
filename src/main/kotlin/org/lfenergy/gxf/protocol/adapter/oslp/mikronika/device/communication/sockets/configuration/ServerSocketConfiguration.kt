package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "server-socket")
class ServerSocketConfiguration {
    var hostName: String = "localhost"
    var port: Int = 12124
}
