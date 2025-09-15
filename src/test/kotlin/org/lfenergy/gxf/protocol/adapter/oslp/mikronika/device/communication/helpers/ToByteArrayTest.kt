package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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

    @Test
    fun `throws exception for zero numBytes`() {
        val value = 0x1234
        assertThrows(IllegalArgumentException::class.java) {
            value.toByteArray(0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            value.toByteArray(5)
        }
    }

    @Test
    fun `throws exception for large number of byte`() {
        val value = 0x1234
        assertThrows(IllegalArgumentException::class.java) {
            value.toByteArray(5)
        }
    }
}
