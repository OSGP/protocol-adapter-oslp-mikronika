// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import com.gxf.utilities.oslp.message.signing.SigningUtil
import com.gxf.utilities.oslp.message.signing.configuration.SigningProperties
import jakarta.persistence.EntityNotFoundException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaRepository
import org.springframework.stereotype.Service

@Service
class SigningService(
    private val keyProvider: KeyProvider,
    signingConfiguration: SigningConfiguration,
    private val mikronikaRepository: MikronikaRepository,
) : SigningUtil(SigningProperties(signingConfiguration.securityProvider, signingConfiguration.securityAlgorithm)) {
    fun createSignature(data: ByteArray): ByteArray = this.createSignature(data, keyProvider.getPrivateKey())

    fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        deviceIdentification: String,
    ): Boolean {
        val device: MikronikaDevice =
            mikronikaRepository.findByDeviceIdentification(deviceIdentification)
                ?: throw EntityNotFoundException("Device with identification $deviceIdentification not found")
        return this.verifySignature(
            data,
            signature,
            keyProvider.getPublicKey(device),
        )
    }
}
