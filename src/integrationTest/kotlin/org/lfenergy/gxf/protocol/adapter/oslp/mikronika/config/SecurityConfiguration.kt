// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.KeyProvider
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

@TestConfiguration
class SecurityConfiguration(
    private val signingConfigurationProperties: SigningConfigurationProperties,
) {
    @Bean
    fun deviceKeyPair(): KeyPair =
        KeyPairGenerator
            .getInstance("EC")
            .apply { initialize(256) }
            .genKeyPair()

    @Bean
    fun platformKeyPair(): KeyPair =
        KeyPairGenerator
            .getInstance("EC")
            .apply { this.initialize(256) }
            .genKeyPair()

    @Bean
    fun keyProvider(): KeyProvider =
        object : KeyProvider(signingConfigurationProperties) {
            override fun getPrivateKey(): PrivateKey = platformKeyPair().private
        }
}

fun PublicKey.encodedAsBase64(): String =
    java.util.Base64
        .getEncoder()
        .encodeToString(this.encoded)
