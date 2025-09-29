// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain

import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message

@ExtendWith(MockKExtension::class)
class EnvelopeTest {
    @Test
    fun `get message should return the message`() {
        val envelope =
            Envelope(
                securityKey = ByteArray(SECURITY_KEY_LENGTH) { 1 },
                sequenceNumber = 42,
                deviceUid = ByteArray(DEVICE_UID_LENGTH) { 2 },
                lengthIndicator = 10,
                messageBytes = testMessage.toByteArray(),
            )

        val actual = envelope.message

        assertThat(actual).isEqualTo(testMessage)
    }

    @Test
    fun `getBytes should return correct byte array layout`() {
        val securityKey = ByteArray(SECURITY_KEY_LENGTH) { 0x01 }
        val sequenceNumber = 0x2A // 42
        val deviceUid = ByteArray(DEVICE_UID_LENGTH) { 0x02 }
        val lengthIndicator = 0x000A // 10
        val messageBytes = ByteArray(10) { 0x03 }

        val envelope =
            Envelope(
                securityKey = securityKey,
                sequenceNumber = sequenceNumber,
                deviceUid = deviceUid,
                lengthIndicator = lengthIndicator,
                messageBytes = messageBytes,
            )

        val bytes = envelope.getBytes()

        assertThat(bytes.size).isEqualTo(SECURITY_KEY_LENGTH + SEQUENCE_NUMBER_LENGTH + DEVICE_UID_LENGTH + LENGTH_INDICATOR_LENGTH + 10)

        assertThat(bytes.sliceArray(0 until 128)).isEqualTo(securityKey)
        assertThat(bytes.sliceArray(128 until 130)).isEqualTo(sequenceNumber.toByteArray(2))
        assertThat(bytes.sliceArray(130 until 142)).isEqualTo(deviceUid)
        assertThat(bytes.sliceArray(142 until 144)).isEqualTo(lengthIndicator.toByteArray(2))
        assertThat(bytes.sliceArray(144 until 154)).isEqualTo(messageBytes)
    }

    @Test
    fun `Test equals on two of the same envelopes should return true`() {
        val envelope =
            Envelope(
                securityKey = ByteArray(SECURITY_KEY_LENGTH) { 1 },
                sequenceNumber = 42,
                deviceUid = ByteArray(DEVICE_UID_LENGTH) { 2 },
                lengthIndicator = 10,
                messageBytes = testMessage.toByteArray(),
            )

        val envelope2 =
            Envelope(
                securityKey = ByteArray(SECURITY_KEY_LENGTH) { 1 },
                sequenceNumber = 42,
                deviceUid = ByteArray(DEVICE_UID_LENGTH) { 2 },
                lengthIndicator = 10,
                messageBytes = testMessage.toByteArray(),
            )

        assertThat(envelope).isEqualTo(envelope2)
        assertThat(envelope.hashCode()).isEqualTo(envelope2.hashCode())
    }

    @Test
    fun `Test equals on two different envelopes should return false`() {
        val envelope =
            Envelope(
                securityKey = ByteArray(SECURITY_KEY_LENGTH) { 1 },
                sequenceNumber = 1,
                deviceUid = ByteArray(DEVICE_UID_LENGTH) { 2 },
                lengthIndicator = 10,
                messageBytes = testMessage.toByteArray(),
            )

        val envelope2 =
            Envelope(
                securityKey = ByteArray(SECURITY_KEY_LENGTH) { 1 },
                sequenceNumber = 42,
                deviceUid = ByteArray(DEVICE_UID_LENGTH) { 2 },
                lengthIndicator = 10,
                messageBytes = testMessage.toByteArray(),
            )

        assertThat(envelope).isNotEqualTo(envelope2)
        assertThat(envelope.hashCode()).isNotEqualTo(envelope2.hashCode())
    }

    @Test
    fun `parseFrom should correctly parse byte array into Envelope`() {
        val securityKey = ByteArray(SECURITY_KEY_LENGTH) { 0x01 }
        val sequenceNumber = 0x2A // 42
        val deviceUid = ByteArray(DEVICE_UID_LENGTH) { 0x02 }
        val lengthIndicator = 0x000A // 10
        val messageBytes = ByteArray(10) { 0x03 }

        val bytes =
            ByteArray(SECURITY_KEY_LENGTH + SEQUENCE_NUMBER_LENGTH + DEVICE_UID_LENGTH + LENGTH_INDICATOR_LENGTH + 10)
        securityKey.copyInto(bytes, 0)
        sequenceNumber.toByteArray(2).copyInto(bytes, 128)
        deviceUid.copyInto(bytes, 130)
        lengthIndicator.toByteArray(2).copyInto(bytes, 142)
        messageBytes.copyInto(bytes, 144)

        val envelope = Envelope.parseFrom(bytes)

        assertThat(envelope.securityKey).isEqualTo(securityKey)
        assertThat(envelope.sequenceNumber).isEqualTo(sequenceNumber)
        assertThat(envelope.deviceUid).isEqualTo(deviceUid)
        assertThat(envelope.lengthIndicator).isEqualTo(lengthIndicator)
        assertThat(envelope.messageBytes).isEqualTo(messageBytes)
    }

    companion object {
        const val SECURITY_KEY_LENGTH = 128
        const val DEVICE_UID_LENGTH = 12
        const val SEQUENCE_NUMBER_LENGTH = 2
        const val LENGTH_INDICATOR_LENGTH = 2
        val testMessage =
            Message
                .newBuilder()
                .setEventNotificationResponse(Oslp.EventNotificationResponse.newBuilder().setStatus(Oslp.Status.OK))
                .build()
    }
}
