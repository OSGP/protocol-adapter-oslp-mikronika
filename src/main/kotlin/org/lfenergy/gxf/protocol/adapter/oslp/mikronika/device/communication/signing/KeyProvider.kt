// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.PrivateKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.PublicKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaDevicePublicKey
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Component
class KeyProvider(
    private val signingConfiguration: SigningConfiguration,
) {
    private val logger = KotlinLogging.logger {}

    fun getPrivateKey(): PrivateKey {
        val bytes = Files.readAllBytes(File(signingConfiguration.privateKeyPath).toPath())
        try {
            val privateKeySpec = PKCS8EncodedKeySpec(bytes)
            val privateKeyFactory =
                KeyFactory.getInstance(signingConfiguration.securityKeyType, signingConfiguration.securityProvider)
            return privateKeyFactory.generatePrivate(privateKeySpec)
        } catch (ex: GeneralSecurityException) {
            val exception =
                PrivateKeyException(
                    "Security exception creating private key for algorithm ${signingConfiguration.securityAlgorithm} by provider ${signingConfiguration.securityProvider} and path ${signingConfiguration.privateKeyPath}, with message: ${ex.message}",
                )
            logger.error { exception.message }
            throw exception
        }
    }

    fun getPublicKey(publicMikronikaDevicePublicKey: MikronikaDevicePublicKey): PublicKey {
        try {
            val publicKeyBytes =
                Base64
                    .getDecoder()
                    .decode(publicMikronikaDevicePublicKey.keyPath)
            val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
            val publicKeyFactory =
                KeyFactory.getInstance(signingConfiguration.securityKeyType, signingConfiguration.securityProvider)
            return publicKeyFactory.generatePublic(publicKeySpec)
        } catch (ex: GeneralSecurityException) {
            val exception =
                PublicKeyException(
                    "Security exception creating public key for algorithm ${signingConfiguration.securityAlgorithm} by provider ${signingConfiguration.securityProvider}, with message: ${ex.message}",
                )
            logger.error { exception.message }
            throw exception
        }
    }
}
