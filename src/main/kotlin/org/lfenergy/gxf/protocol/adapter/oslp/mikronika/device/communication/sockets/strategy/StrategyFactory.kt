package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.springframework.stereotype.Component

@Component
class StrategyFactory(strategies: Map<String, ReceiveStrategy>) {
    private val strategyMap = strategies

    fun getStrategy(messageType: String): ReceiveStrategy? = strategyMap[messageType]
}
