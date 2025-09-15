// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ToByteArrayTest {
    @Test
    fun `converts Int to ByteArray with 1 byte`() {
        val value = 0x7F
        val result = value.toByteArray(1)
        assertArrayEquals(byteArrayOf(0x7F.toByte()), result)
    }

    @Test
    fun `converts Int to ByteArray with 2 bytes`() {
        val value = 0x1234
        val result = value.toByteArray(2)
        assertArrayEquals(byteArrayOf(0x12, 0x34), result)
    }

    @Test
    fun `converts Int to ByteArray with 4 bytes`() {
        val value = 0x01020304
        val result = value.toByteArray(4)
        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04), result)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 5])
    fun `throws exception for invalid numBytes`(numBytes: Int) {
        val value = 0x1234
        assertThatThrownBy { value.toByteArray(numBytes) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
