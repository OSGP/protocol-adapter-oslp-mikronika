// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.springframework.stereotype.Component

@Component
class StrategyFactory(
    strategies: Map<String, ReceiveStrategy>,
) {
    private val strategyMap = strategies

    fun getStrategy(messageType: String): ReceiveStrategy =
        strategyMap[messageType] ?: throw InvalidRequestException("Unable to find the correct strategy for the message")

    companion object {
        const val REGISTER_DEVICE_STRATEGY = "RegisterDeviceStrategy"
        const val CONFIRM_REGISTER_DEVICE_STRATEGY = "ConfirmRegisterDeviceStrategy"
        const val EVENT_NOTIFICATION_STRATEGY = "EventNotificationRequestStrategy"
    }
}
