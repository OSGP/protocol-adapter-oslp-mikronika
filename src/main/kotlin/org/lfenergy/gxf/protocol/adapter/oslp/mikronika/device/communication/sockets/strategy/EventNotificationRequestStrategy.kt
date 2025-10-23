// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.strategy.StrategyFactory.Companion.EVENT_NOTIFICATION_STRATEGY
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Component

@Component(EVENT_NOTIFICATION_STRATEGY)
class EventNotificationRequestStrategy(
    signingService: SigningService,
    mikronikaDeviceService: MikronikaDeviceService,
) : ReceiveStrategy(signingService, mikronikaDeviceService) {
    override fun handle(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ) {
        mikronikaDevice.sequenceNumber = requestEnvelope.sequenceNumber
    }

    override fun buildResponsePayload(
        requestEnvelope: Envelope,
        mikronikaDevice: MikronikaDevice,
    ): Message =
        Message
            .newBuilder()
            .setEventNotificationResponse(Oslp.EventNotificationResponse.newBuilder().setStatus(Oslp.Status.OK))
            .build()
}
