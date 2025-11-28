package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.deviceGetStatusRequestMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.GetStatusCommandMapper
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.sender.DeviceResponseSender
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetStatusRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType

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

    @InjectMockKs
    private lateinit var subject: DeviceRequestService

    @Test
    fun `should call mapper and deviceClientService`() {
        val getStatusRequest: DeviceRequest = GetStatusRequest("", "")
        every { mapperFactory.getMapperFor(any()) } returns getStatusCommandMapper
        every { getStatusCommandMapper.toInternal(any()) } returns getStatusRequest
        every { deviceClientService.sendClientMessage(any(), any()) } just Runs

        subject.handleDeviceRequestMessage(deviceGetStatusRequestMessage)

        verify {
            mapperFactory.getMapperFor(RequestType.GET_STATUS_REQUEST)
            getStatusCommandMapper.toInternal(deviceGetStatusRequestMessage)
            deviceClientService.sendClientMessage(getStatusRequest, any())
        }
    }

}
