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
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceGetConfigurationResponseMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceGetStatusRequestMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceSetConfigurationResponseMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.GetConfigurationCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.SetConfigurationCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetConfigurationRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.SetConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage

@ExtendWith(MockKExtension::class)
class SetConfigurationRequestServiceTest {
    @MockK
    private lateinit var deviceClientService: DeviceClientService

    @MockK
    private lateinit var deviceResponseSender: DeviceResponseSender

    @MockK
    private lateinit var getConfigurationCommandMapper: GetConfigurationCommandMapper

    @MockK
    private lateinit var setConfigurationCommandMapper: SetConfigurationCommandMapper

    @InjectMockKs
    private lateinit var subject: SetConfigurationRequestService

    @Test
    fun `should call setConfiguration with data from the getConfigurationRequest`() {
        val getConfigurationRequest: DeviceRequest = GetConfigurationRequest("", "")
        every { getConfigurationCommandMapper.toInternal(any()) } returns getConfigurationRequest
        every { getConfigurationCommandMapper.toResponse(any(), any()) } returns deviceGetConfigurationResponseMessage

        val setConfigurationRequest: SetConfigurationRequest = mockk()
        every { setConfigurationCommandMapper.toInternal(any(), any()) } returns setConfigurationRequest
        every { setConfigurationCommandMapper.toResponse(any(), any()) } returns deviceSetConfigurationResponseMessage
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleSetConfigurationRequest(deviceGetStatusRequestMessage)

        val getConfigResponseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            getConfigurationCommandMapper.toInternal(deviceGetStatusRequestMessage)
            deviceClientService.sendClientMessage(getConfigurationRequest, capture(getConfigResponseMapperSlot))
        }

        val envelope: Envelope = mockk()
        getConfigResponseMapperSlot.captured.invoke(Result.success(envelope))

        val setConfigResponseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            setConfigurationCommandMapper.toInternal(any(), any())
            deviceClientService.sendClientMessage(setConfigurationRequest, capture(setConfigResponseMapperSlot))
        }

        setConfigResponseMapperSlot.captured.invoke(Result.success(envelope))

        verify {
            setConfigurationCommandMapper.toResponse(any(), envelope)
            deviceResponseSender.send(deviceSetConfigurationResponseMessage)
        }
    }

    @Test
    fun `should call deviceClientService with error message if getConfiguration fails failed`() {
        val getConfigurationRequest: DeviceRequest = GetConfigurationRequest("", "")
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs
        every { getConfigurationCommandMapper.toInternal(any()) } returns getConfigurationRequest

        subject.handleSetConfigurationRequest(deviceGetStatusRequestMessage)

        val responseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            deviceClientService.sendClientMessage(getConfigurationRequest, capture(responseMapperSlot))
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

    @Test
    fun `should send error response if setConfiguration fails`() {
        val getConfigurationRequest: DeviceRequest = GetConfigurationRequest("", "")
        every { getConfigurationCommandMapper.toInternal(any()) } returns getConfigurationRequest
        every { getConfigurationCommandMapper.toResponse(any(), any()) } returns deviceGetConfigurationResponseMessage

        val setConfigurationRequest: SetConfigurationRequest = mockk()
        every { setConfigurationCommandMapper.toInternal(any(), any()) } returns setConfigurationRequest
        every { setConfigurationCommandMapper.toResponse(any(), any()) } returns deviceSetConfigurationResponseMessage
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleSetConfigurationRequest(deviceGetStatusRequestMessage)

        val getConfigResponseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            deviceClientService.sendClientMessage(getConfigurationRequest, capture(getConfigResponseMapperSlot))
        }

        val envelope: Envelope = mockk()
        getConfigResponseMapperSlot.captured.invoke(Result.success(envelope))

        val setConfigResponseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            deviceClientService.sendClientMessage(setConfigurationRequest, capture(setConfigResponseMapperSlot))
        }

        val exception = Exception("Set configuration failed")
        setConfigResponseMapperSlot.captured.invoke(Result.failure(exception))

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
