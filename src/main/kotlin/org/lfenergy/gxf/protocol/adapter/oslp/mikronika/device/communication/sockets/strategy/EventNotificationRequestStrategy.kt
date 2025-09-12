package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDevice
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component

@Component("EventNotificationRequestStrategy")
class EventNotificationRequestStrategy(
    signingService: SigningService,
    mikronikaDeviceService: MikronikaDeviceService
) :
    ReceiveStrategy(signingService, mikronikaDeviceService) {

    override fun handle(requestEnvelope: Envelope, mikronikaDevice: MikronikaDevice) {
        mikronikaDevice.sequenceNumber = requestEnvelope.sequenceNumber
    }

    override fun buildResponsePayload(requestEnvelope: Envelope, mikronikaDevice: MikronikaDevice): Message {
        return Message.newBuilder()
            .setEventNotificationResponse(Oslp.EventNotificationResponse.newBuilder().setStatus(Oslp.Status.OK))
            .build()
    }
}
