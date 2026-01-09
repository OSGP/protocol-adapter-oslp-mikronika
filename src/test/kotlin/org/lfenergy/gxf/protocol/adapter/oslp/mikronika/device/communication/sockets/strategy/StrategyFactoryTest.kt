// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server.strategy.ReceiveStrategy
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.server.strategy.StrategyFactory

class StrategyFactoryTest {
    @Test
    fun `getStrategy returns correct strategy for key`() {
        val strategyA = mockk<ReceiveStrategy>()
        val strategyB = mockk<ReceiveStrategy>()

        val strategies = mapOf("A" to strategyA, "B" to strategyB)
        val factory = StrategyFactory(strategies)

        assertThat(strategyA).isSameAs(factory.getStrategy("A"))
        assertThat(strategyB).isSameAs(factory.getStrategy("B"))
    }

    @Test
    fun `getStrategy throws exception for unknown key`() {
        val strategyA = mockk<ReceiveStrategy>()
        val strategies = mapOf("A" to strategyA)
        val factory = StrategyFactory(strategies)

        assertThatThrownBy { factory.getStrategy("Unknown") }
            .isInstanceOf(InvalidRequestException::class.java)
            .hasMessageContaining("Unable to find the correct strategy for the message")
    }
}
