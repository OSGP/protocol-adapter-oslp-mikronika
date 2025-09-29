// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import com.gxf.utilities.oslp.message.signing.SigningUtil
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaDevicePublicKey
import java.security.PrivateKey
import java.security.PublicKey

@ExtendWith(MockKExtension::class)
class SigningServiceTest {
    @MockK
    lateinit var keyProvider: KeyProvider

    @MockK
    lateinit var signingUtil: SigningUtil

    @InjectMockKs
    lateinit var signingService: SigningService

    @Test
    fun `createSignature should call the signingUtil`() {
        val privateKey = mockk<PrivateKey>()
        every { keyProvider.getPrivateKey() } returns privateKey

        val data = byteArrayOf(1, 2, 3)

        every { signingUtil.createSignature(any(), any()) } returns data
        signingService.createSignature(data)

        verify { signingUtil.createSignature(data, privateKey) }
    }

    @Test
    fun `verify signature should call the signingUtil`() {
        val data = byteArrayOf(1, 2, 3)
        val signature = byteArrayOf(4, 5, 6)
        val mikronikaDevicePublicKey = mockk<MikronikaDevicePublicKey>()

        val publicKey = mockk<PublicKey>()
        every { keyProvider.getPublicKey(mikronikaDevicePublicKey) } returns publicKey

        every { signingUtil.verifySignature(data, signature, publicKey) } returns true

        val result = signingService.verifySignature(data, signature, mikronikaDevicePublicKey)

        assertThat(result).isTrue()
        verify { signingUtil.verifySignature(data, signature, publicKey) }
    }
}
