// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

@Configuration
@Profile("local")
class LocalContainerConfig {
    companion object {
        private val postgresContainer =
            PostgreSQLContainer("postgres:17-alpine").apply {
                withDatabaseName("osgp_adapter_protocol_oslp_mikronika")
                withUsername("osp_admin")
                withPassword("test")
                portBindings = listOf("51360:5432")
                start()
            }

        private val artemisContainer =
            GenericContainer<Nothing>(DockerImageName.parse("apache/activemq-artemis:2.41.0-alpine")).apply {
                withEnv("ARTEMIS_USER", "artemis") // default user
                withEnv("ARTEMIS_PASSWORD", "artemis") // default password
                portBindings = listOf("52360:8161", "61616:61616")
                waitingFor(Wait.forListeningPort())
                start()
            }
    }
}
