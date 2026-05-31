// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client.TcpClientFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DeviceTcpClientConfigurationProperties::class)
class DeviceClientConfiguration {
    @Bean
    fun tcpClientFactory(configuration: DeviceTcpClientConfigurationProperties): TcpClientFactory =
        TcpClientFactory {
            configuration.proxy?.also { proxyConfig ->
                proxy {
                    host = proxyConfig.host
                    port = proxyConfig.port
                }
            }
            configuration.ssl?.also { config ->
                ssl {
                    keyStorePath = config.keyStorePath
                    trustStorePath = config.trustStorePath
                }
            }
        }
}
