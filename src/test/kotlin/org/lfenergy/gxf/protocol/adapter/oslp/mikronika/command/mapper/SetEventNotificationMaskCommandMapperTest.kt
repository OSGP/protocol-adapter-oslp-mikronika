// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetEventNotificationMaskRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.NotificationType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setEventNotificationMaskRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setEventNotificationsResponse
import kotlin.test.assertEquals

class SetEventNotificationMaskCommandMapperTest {
    private val subject: SetEventNotificationMaskCommandMapper = SetEventNotificationMaskCommandMapper()

    @Test
    fun `should map toInternal correctly`() {
        val deviceRequestMessage =
            deviceRequestMessage {
                header = requestHeader
                setEventNotificationMaskRequest =
                    setEventNotificationMaskRequest {
                        notificationTypes.addAll(
                            listOf(
                                NotificationType.TARIFF_EVENTS,
                                NotificationType.MONITOR_EVENTS,
                            ),
                        )
                    }
            }

        val result = subject.toInternal(deviceRequestMessage) as SetEventNotificationMaskRequest

        assertEquals(DEVICE_IDENTIFICATION, result.device.deviceIdentification)
        assertEquals(NETWORK_ADDRESS, result.device.networkAddress)
        assertEquals(24, result.notificationMask)
    }

    @Test
    fun `should map toResponse correctly`() {
        val envelope = mockk<Envelope>()

        val message =
            message {
                setEventNotificationsResponse =
                    setEventNotificationsResponse {
                        status = Oslp.Status.OK
                    }
            }

        every { envelope.message } returns message

        val result = subject.toResponse(requestHeader, envelope)

        assertRequestHeader(result)

        assertEquals(Result.OK, result.result)
    }
}
