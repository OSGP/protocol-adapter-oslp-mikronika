// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidRequestException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.ReceiveStrategy
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory.Companion.CONFIRM_REGISTER_DEVICE_STRATEGY
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory.Companion.EVENT_NOTIFICATION_STRATEGY
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory.Companion.REGISTER_DEVICE_STRATEGY
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component

@Component
class ServerSocketMessageProcessor(
    private val strategyFactory: StrategyFactory,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun handleMessage(
        envelope: Envelope,
        output: ByteWriteChannel,
    ) {
        logger.info {
            "Received: Seq: ${envelope.sequenceNumber} - Len: ${envelope.lengthIndicator} Message: ${envelope.message}"
        }

        val message = envelope.message
        try {
            getStrategyFor(message).let {
                it
                    .invoke(
                        envelope,
                    )?.let { envelope ->
                        val responseBytes = envelope.getBytes()
                        output.writeFully(responseBytes)
                    }
            }
        } catch (invalidRequestException: InvalidRequestException) {
            logger.error { "Ignoring incoming request because of ${invalidRequestException.message}" }
        }
    }

    private fun getStrategyFor(message: Message): ReceiveStrategy {
        with(message) {
            return when {
                hasRegisterDeviceRequest() -> strategyFactory.getStrategy(REGISTER_DEVICE_STRATEGY)
                hasConfirmRegisterDeviceRequest() -> strategyFactory.getStrategy(CONFIRM_REGISTER_DEVICE_STRATEGY)
                hasEventNotificationRequest() -> strategyFactory.getStrategy(EVENT_NOTIFICATION_STRATEGY)
                else -> error("Unexpected request message: $message")
            }
        }
    }
}
