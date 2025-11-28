package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.GET_STATUS_REQUEST
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import kotlin.test.assertEquals

class CommandMapperFactoryTest {

    @Test
    fun `should return correct mapper through requestType enum`() {
        val mapper1 = mockk<CommandMapper>()
        val mapper2 = mockk<CommandMapper>()
        val subject = CommandMapperFactory(
            hashMapOf(
                GET_STATUS_REQUEST to mapper1,
                "SOME_OTHER_VALUE" to mapper2,
            )
        )

        val result = subject.getMapperFor(RequestType.GET_STATUS_REQUEST)

        assertEquals(result, mapper1)
    }
}
