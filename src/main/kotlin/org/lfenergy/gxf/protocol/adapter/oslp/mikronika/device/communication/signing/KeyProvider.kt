// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.PrivateKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.PublicKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

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
            ex.printStackTrace()
            val exception =
                PrivateKeyException(
                    "Security exception creating private key for algorithm ${signingConfiguration.securityAlgorithm} by provider ${signingConfiguration.securityProvider} and path ${signingConfiguration.privateKeyPath}",
                )
            logger.error { exception.message }
            throw exception
        }
    }

    fun getPublicKey(mikronikaDevice: MikronikaDevice): PublicKey {
        try {
            val publicKeyBase64 = mikronikaDevice.publicKey
            val publicKeyBytes = Base64.getDecoder()
                .decode(publicKeyBase64)
            val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
            val publicKeyFactory =
                KeyFactory.getInstance(signingConfiguration.securityKeyType, signingConfiguration.securityProvider)
            return publicKeyFactory.generatePublic(publicKeySpec)
        } catch (ex: GeneralSecurityException) {
            ex.printStackTrace()
            val exception =
                PublicKeyException(
                    "Security exception creating public key for algorithm ${signingConfiguration.securityAlgorithm} by provider ${signingConfiguration.securityProvider} and device ${mikronikaDevice.deviceIdentification}",
                )
            logger.error { exception.message }
            throw exception
        }
    }
}
