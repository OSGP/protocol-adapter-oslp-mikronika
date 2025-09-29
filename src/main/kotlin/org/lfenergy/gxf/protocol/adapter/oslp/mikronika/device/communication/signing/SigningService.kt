// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import com.gxf.utilities.oslp.message.signing.SigningUtil
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaDevicePublicKey
import org.springframework.stereotype.Service

@Service
class SigningService(
    private val keyProvider: KeyProvider,
    private val signingUtil: SigningUtil,
) {
    fun createSignature(data: ByteArray): ByteArray = signingUtil.createSignature(data, keyProvider.getPrivateKey())

    fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        mikronikaDevicePublicKey: MikronikaDevicePublicKey,
    ): Boolean =
        signingUtil.verifySignature(
            data,
            signature,
            keyProvider.getPublicKey(mikronikaDevicePublicKey),
        )
}
