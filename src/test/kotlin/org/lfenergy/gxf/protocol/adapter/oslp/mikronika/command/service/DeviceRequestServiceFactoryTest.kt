// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import io.ktor.util.reflect.instanceOf
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request.GenericDeviceRequestService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request.SetLightRequestService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service.request.SetScheduleRequestService
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.deviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.requestHeader
import kotlin.reflect.KClass

@ExtendWith(MockKExtension::class)
class DeviceRequestServiceFactoryTest {
    @MockK
    private lateinit var genericDeviceRequestService: GenericDeviceRequestService

    @MockK
    private lateinit var setLightRequestService: SetLightRequestService

    @MockK
    private lateinit var setScheduleRequestService: SetScheduleRequestService

    @InjectMockKs
    private lateinit var subject: DeviceRequestServiceFactory

    @ParameterizedTest
    @MethodSource("params")
    fun `should return correct instance on request`(
        requestType: RequestType,
        classType: KClass<*>,
    ) {
        val request =
            deviceRequestMessage {
                header =
                    requestHeader {
                        this.requestType = requestType
                    }
            }

        val result = subject.getDeviceRequestServiceFor(request)

        assertThat(
            result.instanceOf(classType),
        ).isTrue
    }

    companion object {
        @JvmStatic
        fun params() =
            listOf(
                Arguments.of(RequestType.GET_STATUS_REQUEST, GenericDeviceRequestService::class),
                Arguments.of(RequestType.SET_LIGHT_REQUEST, SetLightRequestService::class),
                Arguments.of(RequestType.SET_SCHEDULE_REQUEST, SetScheduleRequestService::class),
            )
    }
}
