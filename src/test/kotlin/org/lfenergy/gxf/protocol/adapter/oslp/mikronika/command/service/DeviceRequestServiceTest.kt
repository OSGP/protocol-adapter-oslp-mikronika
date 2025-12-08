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
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceGetStatusRequestMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceGetStatusResponseMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceSetConfigurationRequestMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.GetStatusCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetStatusRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage

@ExtendWith(MockKExtension::class)
class DeviceRequestServiceTest {
    @MockK
    private lateinit var deviceClientService: DeviceClientService

    @MockK
    private lateinit var deviceResponseSender: DeviceResponseSender

    @MockK
    private lateinit var mapperFactory: CommandMapperFactory

    @MockK
    private lateinit var getStatusCommandMapper: GetStatusCommandMapper

    @MockK
    private lateinit var setConfigurationRequestService: SetConfigurationRequestService

    @InjectMockKs
    private lateinit var subject: DeviceRequestService

    @Test
    fun `should call mapper and deviceClientService whenever call is successful`() {
        val getStatusRequest: DeviceRequest = GetStatusRequest("", "")
        every { mapperFactory.getMapperFor(any()) } returns getStatusCommandMapper
        every { getStatusCommandMapper.toInternal(any()) } returns getStatusRequest
        every { getStatusCommandMapper.toResponse(any(), any()) } returns deviceGetStatusResponseMessage
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleDeviceRequestMessage(deviceGetStatusRequestMessage)

        val responseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            mapperFactory.getMapperFor(RequestType.GET_STATUS_REQUEST)
            getStatusCommandMapper.toInternal(deviceGetStatusRequestMessage)
            deviceClientService.sendClientMessage(getStatusRequest, capture(responseMapperSlot))
        }

        val envelope: Envelope = mockk()
        responseMapperSlot.captured.invoke(Result.success(envelope))

        verify {
            getStatusCommandMapper.toResponse(deviceGetStatusRequestMessage.header, envelope)
            deviceResponseSender.send(deviceGetStatusResponseMessage)
        }
    }

    @Test
    fun `should call deviceClientService with error message if call failed`() {
        val getStatusRequest: DeviceRequest = GetStatusRequest("", "")
        every { mapperFactory.getMapperFor(any()) } returns getStatusCommandMapper
        every { getStatusCommandMapper.toInternal(any()) } returns getStatusRequest
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs
        every { deviceResponseSender.send(any()) } just Runs

        subject.handleDeviceRequestMessage(deviceGetStatusRequestMessage)

        val responseMapperSlot = slot<(Result<Envelope>) -> Unit>()
        verify {
            mapperFactory.getMapperFor(RequestType.GET_STATUS_REQUEST)
            getStatusCommandMapper.toInternal(deviceGetStatusRequestMessage)
            deviceClientService.sendClientMessage(getStatusRequest, capture(responseMapperSlot))
        }

        val exception = Exception("Some message")
        responseMapperSlot.captured.invoke(Result.failure(exception))

        val responseErrorMessageSlot = slot<DeviceResponseMessage>()
        verify {
            deviceResponseSender.send(capture(responseErrorMessageSlot))
        }

        val responseErrorMessage = responseErrorMessageSlot.captured
        assertEquals("correlationUid", responseErrorMessage.header.correlationUid)
        assertEquals(DEVICE_IDENTIFICATION, responseErrorMessage.header.deviceIdentification)
        assertEquals("deviceType", responseErrorMessage.header.deviceType)
        assertEquals("organizationIdentification", responseErrorMessage.header.organizationIdentification)
    }

    @Test
    fun `should call setConfigurationRequestService when request is SetConfigurationRequest`() {
        every { setConfigurationRequestService.handleSetConfigurationRequest(deviceSetConfigurationRequestMessage) } just runs
        subject.handleDeviceRequestMessage(deviceSetConfigurationRequestMessage)

        verify { setConfigurationRequestService.handleSetConfigurationRequest(deviceSetConfigurationRequestMessage) }

        verify(exactly = 0) { deviceResponseSender.send(any()) }
    }
}
