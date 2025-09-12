// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers

class ByteArrayHelpers {
    companion object {
        fun Int.toByteArray(numBytes: Int): ByteArray {
            require(numBytes in 1..4) { "numBytes must be between 1 and 4" }
            return ByteArray(numBytes) { i -> (this shr (8 * (numBytes - i - 1)) and 0xFF).toByte() }
        }

        fun Int?.toByteArray(numBytes: Int): ByteArray = this?.toByteArray(numBytes) ?: ByteArray(numBytes) { 0 }
    }
}
