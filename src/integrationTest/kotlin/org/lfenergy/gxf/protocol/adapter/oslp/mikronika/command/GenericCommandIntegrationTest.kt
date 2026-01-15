// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.springframework.beans.factory.annotation.Autowired

class GenericCommandIntegrationTest : CommandIntegrationTest() {
    @Autowired
    private lateinit var commandMapperFactory: CommandMapperFactory

    @Test
    fun `should have mappers for all defined request types`() {
        for (requestType in RequestType.entries.onlyDefinedTypes()) {
            assertThat(commandMapperFactory.getMapperFor(requestType)).isNotNull
        }
    }

    @Test
    fun `should throw exception when unrecognized request is used`() {
        assertThatThrownBy {
            commandMapperFactory.getMapperFor(RequestType.UNRECOGNIZED)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun List<RequestType>.onlyDefinedTypes(): List<RequestType> = filter { it != RequestType.UNRECOGNIZED }
}
