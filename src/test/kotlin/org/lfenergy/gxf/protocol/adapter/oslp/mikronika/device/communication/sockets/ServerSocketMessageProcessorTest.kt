// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets

import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.readAvailable
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.ReceiveStrategy
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory
import org.opensmartgridplatform.oslp.Oslp.Message
import java.util.stream.Stream

class ServerSocketMessageProcessorTest {
    @ParameterizedTest
    @MethodSource("strategyCases")
    fun `handleMessage should invoke correct strategy and write response`(
        strategyName: String,
        configureMessage: (Message) -> Unit,
    ) = runBlocking {
        val mockedMessage = mockk<Message>(relaxed = true)
        configureMessage(mockedMessage)

        val strategyFactory = mockk<StrategyFactory>()
        val receiveStrategy = mockk<ReceiveStrategy>()

        val expectedBytes = byteArrayOf(1, 2, 3)

        val envelope =
            mockk<Envelope> {
                every { sequenceNumber } returns 1
                every { lengthIndicator } returns 10
                every { message } returns mockedMessage
                every { getBytes() } returns expectedBytes
            }

        every { strategyFactory.getStrategy(strategyName) } returns receiveStrategy
        every { receiveStrategy.invoke(any()) } returns envelope

        val processor = ServerSocketMessageProcessor(strategyFactory)

        val output = ByteChannel()
        processor.handleMessage(envelope, output)
        output.close()

        val buffer = ByteArray(expectedBytes.size)
        val written = output.readAvailable(buffer)
        assert(written == expectedBytes.size)
        assert(buffer.contentEquals(expectedBytes))

        verify { strategyFactory.getStrategy(strategyName) }
    }

    @Test
    fun `handleMessage should throw error for unexpected message`() {
        val mockedMessage =
            mockk<Message> {
                every { hasRegisterDeviceRequest() } returns false
                every { hasConfirmRegisterDeviceRequest() } returns false
                every { hasEventNotificationRequest() } returns false
            }

        val strategyFactory = mockk<StrategyFactory>()
        val envelope =
            mockk<Envelope> {
                every { sequenceNumber } returns 1
                every { lengthIndicator } returns 10
                every { message } returns mockedMessage
            }

        val processor = ServerSocketMessageProcessor(strategyFactory)
        val output = ByteChannel()

        assertThrows<IllegalStateException> {
            runBlocking {
                processor.handleMessage(envelope, output)
            }
        }
    }

    companion object {
        @JvmStatic
        fun strategyCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "RegisterDeviceStrategy",
                    { msg: Message -> every { msg.hasRegisterDeviceRequest() } returns true },
                ),
                Arguments.of(
                    "ConfirmRegisterDeviceStrategy",
                    { msg: Message -> every { msg.hasConfirmRegisterDeviceRequest() } returns true },
                ),
                Arguments.of(
                    "EventNotificationRequestStrategy",
                    { msg: Message -> every { msg.hasEventNotificationRequest() } returns true },
                ),
            )
    }
}
