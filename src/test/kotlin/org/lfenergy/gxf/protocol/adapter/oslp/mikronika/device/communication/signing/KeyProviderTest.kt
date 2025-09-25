// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.PrivateKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.PublicKeyException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaDevicePublicKey
import java.nio.file.Files
import java.nio.file.Paths
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@ExtendWith(MockKExtension::class)
class KeyProviderTest {
    @MockK
    lateinit var signingConfig: SigningConfiguration

    @InjectMockKs
    lateinit var keyProvider: KeyProvider

    @BeforeEach
    fun setUp() {
        every { signingConfig.privateKeyPath } returns "/fake/path"
        every { signingConfig.securityKeyType } returns "EC"
        every { signingConfig.securityProvider } returns "SunEC"
        every { signingConfig.securityAlgorithm } returns "SHA256withECDSA"
        mockkStatic(Files::class)
        mockkStatic(KeyFactory::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Files::class)
        unmockkStatic(KeyFactory::class)
    }

    @Test
    fun `getPrivateKey should return PrivateKey`() {
        val privateKeyBytes = byteArrayOf(1, 2, 3)
        every { Files.readAllBytes(Paths.get("/fake/path")) } returns privateKeyBytes

        val keyFactory = mockk<KeyFactory>()
        val privateKey = mockk<PrivateKey>()
        every { KeyFactory.getInstance("EC", "SunEC") } returns keyFactory
        every { keyFactory.generatePrivate(any<PKCS8EncodedKeySpec>()) } returns privateKey

        val result = keyProvider.getPrivateKey()
        Assertions.assertNotNull(result)
    }

    @Test
    fun `getPrivateKey throws PrivateKeyException when security exception is thrown`() {
        val privateKeyBytes = byteArrayOf(1, 2, 3)
        every { Files.readAllBytes(Paths.get("/fake/path")) } returns privateKeyBytes

        every { KeyFactory.getInstance("EC", "SunEC") } throws GeneralSecurityException("Test exception")

        assertThatThrownBy { keyProvider.getPrivateKey() }
            .isInstanceOf(PrivateKeyException::class.java)
            .hasMessageContaining("Security exception creating private key for algorithm")
    }

    @Test
    fun `getPublicKey returns PublicKey`() {
        val mikronikaDevicePublicKey =
            MikronikaDevicePublicKey(Base64.getEncoder().encodeToString(byteArrayOf(4, 5, 6)))

        val keyFactory = mockk<KeyFactory>()
        val publicKey = mockk<PublicKey>()
        every { KeyFactory.getInstance("EC", "SunEC") } returns keyFactory
        every { keyFactory.generatePublic(any<X509EncodedKeySpec>()) } returns publicKey

        val result = keyProvider.getPublicKey(mikronikaDevicePublicKey)
        Assertions.assertNotNull(result)
    }

    @Test
    fun `getPublicKey throws PpublicKeyException when security exception is thrown`() {
        val mikronikaDevicePublicKey =
            MikronikaDevicePublicKey(Base64.getEncoder().encodeToString(byteArrayOf(4, 5, 6)))

        every { KeyFactory.getInstance("EC", "SunEC") } throws GeneralSecurityException("Test exception")

        assertThatThrownBy { keyProvider.getPublicKey(mikronikaDevicePublicKey) }
            .isInstanceOf(PublicKeyException::class.java)
            .hasMessageContaining("Security exception creating public key for algorithm")
    }
}
