// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import java.util.Properties
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core"],
    entityManagerFactoryRef = "coreEntityManagerFactory",
    transactionManagerRef = "coreTransactionManager",
)
class CoreDbConfiguration {
    @Value("\${spring.datasource.secondary.hibernate.ddl-auto:none}")
    private lateinit var ddlAuto: String

    @Bean
    @ConfigurationProperties("spring.datasource.secondary")
    fun coreDataSource(): DataSource = DataSourceBuilder.create().build()

    @Bean
    fun coreEntityManagerFactory(
        @Qualifier("coreDataSource") dataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource
        factory.setPackagesToScan("org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core")

        val vendorAdapter = HibernateJpaVendorAdapter()
        factory.jpaVendorAdapter = vendorAdapter
        factory.setJpaProperties(jpaProperties())

        return factory
    }

    private fun jpaProperties(): Properties =
        Properties().apply {
            setProperty("hibernate.hbm2ddl.auto", ddlAuto)
        }

    @Bean
    fun coreTransactionManager(
        @Qualifier("coreEntityManagerFactory") entityManagerFactory: EntityManagerFactory,
    ): PlatformTransactionManager = JpaTransactionManager(entityManagerFactory)
}
