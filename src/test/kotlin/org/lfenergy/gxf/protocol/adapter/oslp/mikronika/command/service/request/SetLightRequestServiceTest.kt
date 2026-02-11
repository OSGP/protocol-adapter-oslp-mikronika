// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceSetLightRequestMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceSetLightResponseMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.SetLightCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.ResumeScheduleRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetLightRequest

@ExtendWith(MockKExtension::class)
class SetLightRequestServiceTest {
    @MockK
    private lateinit var deviceClientService: DeviceClientService

    @MockK
    private lateinit var deviceResponseSender: DeviceResponseSender

    @MockK
    private lateinit var setLightCommandMapper: SetLightCommandMapper

    @InjectMockKs
    private lateinit var subject: SetLightRequestService

    @Test
    fun `should resume schedule after manual set light request`() {
        val setLightRequest: SetLightRequest = mockk()
        every { setLightCommandMapper.toInternal(any()) } returns setLightRequest
        every { setLightCommandMapper.toResponse(any(), any()) } returns deviceSetLightResponseMessage
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleRequestMessage(deviceSetLightRequestMessage)

        val setLightResponseMapperSlot = slot<(Result<Envelope>) -> Unit>()

        verify {
            setLightCommandMapper.toInternal(deviceSetLightRequestMessage)
            deviceClientService.sendClientMessage(setLightRequest, capture(setLightResponseMapperSlot))
        }

        val envelope: Envelope = mockk()
        setLightResponseMapperSlot.captured.invoke(Result.success(envelope))

        val resumeScheduleRequestSlot = slot<ResumeScheduleRequest>()

        verify {
            deviceClientService.sendClientMessage(capture(resumeScheduleRequestSlot), any())
            deviceResponseSender.send(deviceSetLightResponseMessage)
        }

        val resumeScheduleRequestCapture = resumeScheduleRequestSlot.captured

        assertThat(resumeScheduleRequestCapture.index).isEqualTo(0)
        assertThat(resumeScheduleRequestCapture.immediate).isFalse
    }
}
