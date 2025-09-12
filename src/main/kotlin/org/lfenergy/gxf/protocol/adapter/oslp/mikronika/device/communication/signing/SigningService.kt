// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import com.gxf.utilities.oslp.message.signing.SigningUtil
import com.gxf.utilities.oslp.message.signing.configuration.SigningProperties
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.Key
import org.springframework.stereotype.Service

@Service
class SigningService(
    private val keyProvider: KeyProvider,
    signingConfiguration: SigningConfiguration,
) : SigningUtil(SigningProperties(signingConfiguration.securityProvider, signingConfiguration.securityAlgorithm)) {
    fun createSignature(data: ByteArray): ByteArray = this.createSignature(data, keyProvider.getPrivateKey())

    fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        key: Key,
    ): Boolean =
        this.verifySignature(
            data,
            signature,
            keyProvider.getPublicKey(key),
        )
}
