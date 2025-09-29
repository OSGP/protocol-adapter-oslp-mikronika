// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import com.gxf.utilities.oslp.message.signing.SigningUtil
import com.gxf.utilities.oslp.message.signing.configuration.SigningProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SigningConfiguration {
    @Bean
    fun signingUtil(signingConfiguration: SigningConfigurationProperties): SigningUtil {
        val signingProperties =
            SigningProperties(signingConfiguration.securityProvider, signingConfiguration.securityAlgorithm)
        return SigningUtil(signingProperties)
    }
}
