package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceStateService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component

@Component("EventNotificationRequestStrategy")
class EventNotificationRequestStrategy(signingService: SigningService) : ReceiveStrategy(signingService) {
    private val deviceStateService = DeviceStateService.getInstance()

    override fun handle(requestEnvelope: Envelope) {
        deviceStateService.updateSequenceNumber(requestEnvelope.sequenceNumber)
    }

    override fun buildResponsePayload(requestEnvelope: Envelope): Message {
        return Message.newBuilder()
            .setEventNotificationResponse(Oslp.EventNotificationResponse.newBuilder().setStatus(Oslp.Status.OK))
            .build()
    }
}
