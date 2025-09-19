// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.opensmartgridplatform.oslp.Oslp
import java.nio.ByteBuffer

data class Envelope(
    val securityKey: ByteArray,
    val sequenceNumber: Int,
    val deviceUid: ByteArray,
    val lengthIndicator: Int,
    val messageBytes: ByteArray,
) {
    private var cachedMessage: Oslp.Message? = null

    val message: Oslp.Message
        get() = cachedMessage ?: Oslp.Message.parseFrom(messageBytes).also { cachedMessage = it }

    fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(HEADER_LEN + lengthIndicator)

        buffer.put(securityKey.copyOf(SECURITY_KEY_LEN))
        buffer.put(sequenceNumber.toByteArray(SEQUENCE_NUMBER_LEN))
        buffer.put(deviceUid)
        buffer.put(lengthIndicator.toByteArray(LENGTH_INDICATOR_LEN))
        buffer.put(messageBytes)

        return buffer.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Envelope

        if (sequenceNumber != other.sequenceNumber) return false
        if (lengthIndicator != other.lengthIndicator) return false
        if (!deviceUid.contentEquals(other.deviceUid)) return false
        if (!securityKey.contentEquals(other.securityKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sequenceNumber.hashCode()
        result = HASH_PRIME * result + lengthIndicator.hashCode()
        result = HASH_PRIME * result + deviceUid.contentHashCode()
        result = HASH_PRIME * result + securityKey.contentHashCode()
        return result
    }

    companion object {
        private const val SECURITY_KEY_LEN = 128
        private const val SEQUENCE_NUMBER_LEN = 2
        private const val DEVICE_ID_LEN = 12
        private const val LENGTH_INDICATOR_LEN = 2
        private const val HASH_PRIME = 31

        private const val HEADER_LEN = SECURITY_KEY_LEN + SEQUENCE_NUMBER_LEN + DEVICE_ID_LEN + LENGTH_INDICATOR_LEN

        private val SECURITY_KEY_RANGE = 0 size SECURITY_KEY_LEN
        private val SEQUENCE_NUMBER_RANGE = 128 size SEQUENCE_NUMBER_LEN
        private val DEVICE_ID_RANGE = 130 size DEVICE_ID_LEN
        private val LENGTH_INDICATOR_RANGE = 142 size LENGTH_INDICATOR_LEN

        fun parseFrom(bytes: ByteArray): Envelope {
            val securityKey = bytes.sliceArray(SECURITY_KEY_RANGE)
            val sequenceNumber = bytes.sliceArray(SEQUENCE_NUMBER_RANGE).toInt()
            val deviceId = bytes.sliceArray(DEVICE_ID_RANGE)
            val lengthIndicator = bytes.sliceArray(LENGTH_INDICATOR_RANGE).toInt()
            val payload = bytes.sliceArray(HEADER_LEN until bytes.size)

            return Envelope(securityKey, sequenceNumber, deviceId, lengthIndicator, payload)
        }

        fun ByteArray.toInt(): Int = this.fold(0) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xFF) }

        private infix fun Int.size(int: Int): IntRange = this until (this + int)
    }
}
