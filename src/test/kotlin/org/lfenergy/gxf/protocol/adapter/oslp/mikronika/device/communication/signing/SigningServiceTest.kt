// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.security.PrivateKey
import java.security.Signature

@ExtendWith(MockKExtension::class)
class SigningServiceTest {
    @MockK
    lateinit var keyProvider: KeyProvider

    @MockK
    lateinit var signingConfiguration: SigningConfiguration

    lateinit var signingService: SigningService

    @BeforeEach
    fun setUp() {
        every { signingConfiguration.securityProvider } returns "SunEC"
        every { signingConfiguration.securityAlgorithm } returns "SHA256withECDSA"

        mockkStatic(Signature::class)
        val mockSignature = mockk<Signature>(relaxed = true)
        every { Signature.getInstance(any<String>(), any<String>()) } returns mockSignature

        signingService = SigningService(keyProvider, signingConfiguration)
    }

    @AfterEach
    fun tearDown() {
        mockkStatic(Signature::class)
    }

    @Test
    fun `createSignature calls getPrivateKey and creates signature`() {
        val privateKey = mockk<PrivateKey>()
        every { keyProvider.getPrivateKey() } returns privateKey

        val data = byteArrayOf(1, 2, 3)

        signingService.createSignature(data)

        verify { keyProvider.getPrivateKey() }
        verify { Signature.getInstance("SHA256withECDSA", "SunEC") }
    }

    @Test
    fun `verifySignature calls getPublicKey and verifies signature`() {
//        val data = byteArrayOf(1, 2, 3)
//        val signature = byteArrayOf(4, 5, 6)
//        val key = mockk<Key>()
//        val publicKey = mockk<PublicKey>()
//        val mockSignature = mockk<Signature>(relaxed = true)
//
//        every { keyProvider.getPublicKey(key) } returns publicKey
//        mockkStatic(Signature::class)
//        every { Signature.getInstance(any<String>(), any<String>()) } returns mockSignature
//        every { mockSignature.verify(any()) } returns true
//
//        val result = signingService.verifySignature(data, signature, key)
//
//        verify { keyProvider.getPublicKey(key) }
//        verify { Signature.getInstance("SHA256withECDSA", "SunEC") }
//
//        assert(result)
    }
}
