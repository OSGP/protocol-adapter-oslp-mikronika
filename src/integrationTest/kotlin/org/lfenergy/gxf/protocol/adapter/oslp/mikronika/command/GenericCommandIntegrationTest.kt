// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.springframework.beans.factory.annotation.Autowired

class GenericCommandIntegrationTest : CommandIntegrationTest() {
    @Autowired
    private lateinit var commandMapperFactory: CommandMapperFactory

    @Test
    fun `should have mappers for all defined request types`() {
        for (requestType in RequestType.entries.onlyDefinedTypes()) {
            assertNotNull(commandMapperFactory.getMapperFor(requestType))
        }
    }

    @Test
    fun `should throw exception when unrecognized request is used`() {
        assertThrows<IllegalArgumentException> {
            commandMapperFactory.getMapperFor(RequestType.UNRECOGNIZED)
        }
    }

    private fun List<RequestType>.onlyDefinedTypes(): List<RequestType> = filter { it != RequestType.UNRECOGNIZED }
}
