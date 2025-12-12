// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceSetScheduleRequestMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceSetScheduleRequestMessageWithAstromicalOffsets
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceSetScheduleResponseMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.SetScheduleCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetScheduleRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage

@ExtendWith(MockKExtension::class)
class SetScheduleRequestServiceTest {
    @MockK
    private lateinit var deviceClientService: DeviceClientService

    @MockK
    private lateinit var deviceResponseSender: DeviceResponseSender

    @MockK
    private lateinit var setScheduleCommandMapper: SetScheduleCommandMapper

    @InjectMockKs
    private lateinit var subject: SetScheduleRequestService

    @Test
    fun `should call setConfiguration with data from the getConfigurationRequest`() {
        val setScheduleRequest: SetScheduleRequest = mockk()
        every { setScheduleCommandMapper.toResponse(any(), any()) } returns deviceSetScheduleResponseMessage
        every { setScheduleCommandMapper.toInternal(any()) } returns setScheduleRequest
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleSetScheduleRequest(deviceSetScheduleRequestMessage)

        val setScheduleResponseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            setScheduleCommandMapper.toInternal(deviceSetScheduleRequestMessage)
            deviceClientService.sendClientMessage(setScheduleRequest, capture(setScheduleResponseMapperSlot))
        }

        val envelope: Envelope = mockk()
        setScheduleResponseMapperSlot.captured.invoke(Result.success(envelope))

        verify {
            deviceResponseSender.send(deviceSetScheduleResponseMessage)
        }
    }

    @Test
    fun `should call deviceClientService with error message if setConfiguration fails failed`() {
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleSetScheduleRequest(deviceSetScheduleRequestMessageWithAstromicalOffsets)

        val responseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            deviceClientService.sendClientMessage(any(), capture(responseMapperSlot))
        }

        val exception = Exception("Some message")
        responseMapperSlot.captured.invoke(Result.failure(exception))

        val responseErrorMessageSlot = slot<DeviceResponseMessage>()
        verify {
            deviceResponseSender.send(capture(responseErrorMessageSlot))
        }

        val responseErrorMessage = responseErrorMessageSlot.captured
        verifyErrorResponse(responseErrorMessage)
    }

    private fun verifyErrorResponse(responseErrorMessage: DeviceResponseMessage) {
        assertEquals("correlationUid", responseErrorMessage.header.correlationUid)
        assertEquals(DEVICE_IDENTIFICATION, responseErrorMessage.header.deviceIdentification)
        assertEquals("deviceType", responseErrorMessage.header.deviceType)
        assertEquals("organizationIdentification", responseErrorMessage.header.organizationIdentification)
    }
}
