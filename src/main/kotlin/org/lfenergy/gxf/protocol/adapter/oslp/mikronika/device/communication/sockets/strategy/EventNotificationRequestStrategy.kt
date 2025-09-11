// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.communication.signing.SigningService
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component

@Component
class EventNotificationRequestStrategy(singingService: SigningService): ReceiveStrategy(singingService) {
    private val deviceStateService = DeviceStateService.getInstance()

    override fun matches(message: Message): Boolean = message.hasEventNotificationRequest()

    override fun handle(requestEnvelope: Envelope) {
        deviceStateService.updateSequenceNumber(requestEnvelope.sequenceNumber)
        Logger.logReceive("Received event notification request: ${requestEnvelope.message.eventNotificationRequest}")
    }

    override fun buildResponsePayload(requestEnvelope: Envelope): Message =
        Message
            .newBuilder()
            .setEventNotificationResponse(Oslp.EventNotificationResponse.newBuilder().setStatus(Oslp.Status.OK))
            .build()
}
