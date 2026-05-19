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
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseType.SET_LIGHT_RESPONSE

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

        val setLightResponseHandlerSlot = slot<(Result<Envelope>) -> Unit>()

        verify {
            setLightCommandMapper.toInternal(deviceSetLightRequestMessage)
            deviceClientService.sendClientMessage(setLightRequest, capture(setLightResponseHandlerSlot))
        }

        // Trigger onSuccess of setLight
        val envelope: Envelope = mockk()
        setLightResponseHandlerSlot.captured.invoke(Result.success(envelope))

        val resumeScheduleRequestSlot = slot<ResumeScheduleRequest>()
        val resumeScheduleResponseHandlerSlot = slot<(Result<Envelope>) -> Unit>()

        verify {
            deviceClientService.sendClientMessage(
                capture(resumeScheduleRequestSlot),
                capture(resumeScheduleResponseHandlerSlot),
            )
        }

        val resumeScheduleRequestCapture = resumeScheduleRequestSlot.captured

        assertThat(resumeScheduleRequestCapture.index).isEqualTo(0)
        assertThat(resumeScheduleRequestCapture.immediate).isFalse

        // Trigger onSuccess of resumeSchedule
        resumeScheduleResponseHandlerSlot.captured.invoke(Result.success(envelope))

        verify {
            setLightCommandMapper.toResponse(any(), envelope)
            deviceResponseSender.send(deviceSetLightResponseMessage)
        }
    }

    @Test
    fun `should send error response when setLightRequest fails`() {
        val setLightRequest: SetLightRequest = mockk()
        every { setLightCommandMapper.toInternal(any()) } returns setLightRequest
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleRequestMessage(deviceSetLightRequestMessage)

        val setLightResponseHandlerSlot = slot<(Result<Envelope>) -> Unit>()

        verify {
            deviceClientService.sendClientMessage(setLightRequest, capture(setLightResponseHandlerSlot))
        }

        // Trigger onFailure of setLight
        val envelope: Envelope = mockk()
        setLightResponseHandlerSlot.captured.invoke(Result.failure(RuntimeException("I failed miserably")))

        val errorMessageSlot = slot<DeviceResponseMessage>()

        verify {
            deviceResponseSender.send(capture(errorMessageSlot))
        }

        val errorMessage = errorMessageSlot.captured

        assertThat(errorMessage.header.responseType).isEqualTo(SET_LIGHT_RESPONSE)
        assertThat(errorMessage.hasErrorResponse()).isTrue
        assertThat(errorMessage.errorResponse.errorMessage).isEqualTo("I failed miserably")
    }

    @Test
    fun `should send error response when resumeScheduleRequest fails`() {
        val setLightRequest: SetLightRequest = mockk()
        every { setLightCommandMapper.toInternal(any()) } returns setLightRequest
        every { setLightCommandMapper.toResponse(any(), any()) } returns deviceSetLightResponseMessage
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleRequestMessage(deviceSetLightRequestMessage)

        val setLightResponseHandlerSlot = slot<(Result<Envelope>) -> Unit>()

        verify {
            setLightCommandMapper.toInternal(deviceSetLightRequestMessage)
            deviceClientService.sendClientMessage(setLightRequest, capture(setLightResponseHandlerSlot))
        }

        // Trigger onSuccess of setLight
        val envelope: Envelope = mockk()
        setLightResponseHandlerSlot.captured.invoke(Result.success(envelope))

        val resumeScheduleRequestSlot = slot<ResumeScheduleRequest>()
        val resumeScheduleResponseHandlerSlot = slot<(Result<Envelope>) -> Unit>()

        verify {
            deviceClientService.sendClientMessage(
                capture(resumeScheduleRequestSlot),
                capture(resumeScheduleResponseHandlerSlot),
            )
        }

        val resumeScheduleRequestCapture = resumeScheduleRequestSlot.captured

        assertThat(resumeScheduleRequestCapture.index).isEqualTo(0)
        assertThat(resumeScheduleRequestCapture.immediate).isFalse

        // Trigger onFailure of resumeSchedule
        resumeScheduleResponseHandlerSlot.captured.invoke(Result.failure(RuntimeException("I failed miserably")))

        val errorMessageSlot = slot<DeviceResponseMessage>()

        verify {
            deviceResponseSender.send(capture(errorMessageSlot))
        }

        val errorMessage = errorMessageSlot.captured

        assertThat(errorMessage.header.responseType).isEqualTo(SET_LIGHT_RESPONSE)
        assertThat(errorMessage.hasErrorResponse()).isTrue
        assertThat(errorMessage.errorResponse.errorMessage).isEqualTo("I failed miserably")
    }
}
