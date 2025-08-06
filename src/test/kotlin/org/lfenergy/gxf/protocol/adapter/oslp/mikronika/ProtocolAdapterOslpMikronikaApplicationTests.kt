// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest
class ProtocolAdapterOslpMikronikaApplicationTests {
    companion object {
        val postgres =
            PostgreSQLContainer<Nothing>("postgres:16.2").apply {
                withDatabaseName("osgp_adapter_protocol_oslp_mikronika")
                withUsername("osp_admin")
                withPassword("test")
                start()
            }

        @JvmStatic
        @DynamicPropertySource
        fun registerPgProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", ProtocolAdapterOslpMikronikaApplicationTests.Companion.postgres::getJdbcUrl)
            registry.add("spring.datasource.username", ProtocolAdapterOslpMikronikaApplicationTests.Companion.postgres::getUsername)
            registry.add("spring.datasource.password", ProtocolAdapterOslpMikronikaApplicationTests.Companion.postgres::getPassword)
            registry.add(
                "spring.datasource.driver-class-name",
                ProtocolAdapterOslpMikronikaApplicationTests.Companion.postgres::getDriverClassName,
            )
        }
    }

    @Test
    fun contextLoads() {
        // Test startUp application
    }
}
