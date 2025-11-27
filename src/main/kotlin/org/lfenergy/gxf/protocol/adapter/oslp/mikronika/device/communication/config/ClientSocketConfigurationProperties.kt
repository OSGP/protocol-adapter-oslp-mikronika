package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("client-socket")
class ClientSocketConfigurationProperties {
    var devicePort: Int = 12122
}
