// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.exception.PrivateKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.exception.PublicKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.PrivateKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.PublicKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDeviceEntity
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
            val privateKeyFactory = KeyFactory.getInstance(signingConfiguration.securityAlgorithm, signingConfiguration.securityProvider)
            return privateKeyFactory.generatePrivate(privateKeySpec)
        } catch (_: GeneralSecurityException) {
            val exception =
                PrivateKeyException(
                    "Security exception creating private key for algorithm ${signingConfiguration.securityAlgorithm} by provider ${signingConfiguration.securityProvider} and path ${signingConfiguration.privateKeyPath}",
                )
            logger.error { exception.message }
            throw exception
        }
    }

    fun getPublicKey(mikronikaDeviceEntity: MikronikaDeviceEntity): PublicKey {
        try {
            val publicKeyBase64 = mikronikaDeviceEntity.publicKey
            val publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64)
            val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
            val publicKeyFactory = KeyFactory.getInstance(signingConfiguration.securityAlgorithm, signingConfiguration.securityProvider)
            return publicKeyFactory.generatePublic(publicKeySpec)
        } catch (_: GeneralSecurityException) {
            val exception =
                PublicKeyException(
                    "Security exception creating public key for algorithm ${signingConfiguration.securityAlgorithm} by provider ${signingConfiguration.securityProvider} and device ${mikronikaDeviceEntity.deviceIdentification}",
                )
            logger.error { exception.message }
            throw exception
        }
    }
}
