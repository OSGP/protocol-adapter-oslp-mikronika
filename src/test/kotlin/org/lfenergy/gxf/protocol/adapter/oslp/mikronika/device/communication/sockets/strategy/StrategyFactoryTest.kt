package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
    fun `getStrategy returns null for unknown key`() {
        val strategyA = mockk<ReceiveStrategy>()
        val strategies = mapOf("A" to strategyA)
        val factory = StrategyFactory(strategies)

        assertThat(factory.getStrategy("Unknown")).isNull()
    }
}
