// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.activemq.ArtemisContainer
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@TestConfiguration
class ContainerConfiguration {
    @Bean
    @ServiceConnection
    fun artemisContainer() =
        ArtemisContainer("apache/activemq-artemis:2.30.0-alpine").apply {
        }

    @Bean
    @ServiceConnection("primary")
    fun protocolAdapterPostgresqlContainer() =
        PostgreSQLContainer("postgres:15-alpine").apply {
            withDatabaseName("osgp_adapter_protocol_olsp_mikronika")
            withUsername("osp_admin")
            withPassword("1234")
        }

    @Bean
    @ServiceConnection("secondary")
    fun osgpCorePostgresqlContainer() =
        PostgreSQLContainer("postgres:15-alpine").apply {
            withInitScript("db/init-osgp-core-db.sql")
            withDatabaseName("osgp_core")
            withUsername("osp_admin")
            withPassword("1234")
        }

    @Bean
    fun adapterDataSource(): DataSource {
        val container = protocolAdapterPostgresqlContainer()
        return DriverManagerDataSource(container.jdbcUrl, container.username, container.password)
    }

    @Bean
    fun coreDataSource(): DataSource {
        val container = osgpCorePostgresqlContainer()
        return DriverManagerDataSource(container.jdbcUrl, container.username, container.password)
    }

    @Bean
    fun adapterJdbcTemplate(adapterDataSource: DataSource) = JdbcTemplate(adapterDataSource)

    @Bean
    fun coreJdbcTemplate(coreDataSource: DataSource) = JdbcTemplate(coreDataSource)
}
