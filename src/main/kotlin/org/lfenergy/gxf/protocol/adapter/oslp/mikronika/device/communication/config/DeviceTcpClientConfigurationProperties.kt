// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("device-tcp-client")
class DeviceTcpClientConfigurationProperties {
    var devicePort: Int = 12125
    var proxy: ProxyConfigurationProperties? = null
    var ssl: SslConfigurationProperties? = null

    class ProxyConfigurationProperties {
        var host: String = ""
        var port: Int = 0
    }

    class SslConfigurationProperties {
        var keyStorePath: String = ""
        var trustStorePath: String = ""
    }
}
