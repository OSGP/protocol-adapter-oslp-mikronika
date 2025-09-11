// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import com.gxf.utilities.oslp.message.signing.configuration.SigningProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "signing")
class SigningConfiguration {
    lateinit var securityProvider: String
    lateinit var securityAlgorithm: String
    lateinit var securityKeyType: String
    lateinit var privateKeyPath: String
}
