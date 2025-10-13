// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import java.util.Properties
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter"],
    entityManagerFactoryRef = "adapterEntityManagerFactory",
    transactionManagerRef = "adapterTransactionManager",
)
class AdapterDbConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    fun adapterDataSource(): DataSource = DataSourceBuilder.create().build()

    @Bean
    @Primary
    fun adapterEntityManagerFactory(
        @Qualifier("adapterDataSource") dataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource
        factory.setPackagesToScan("org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter")

        val vendorAdapter = HibernateJpaVendorAdapter()
        factory.jpaVendorAdapter = vendorAdapter
        factory.setJpaProperties(jpaProperties())

        return factory
    }

    private fun jpaProperties(): Properties =
        Properties().apply {
            setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        }

    @Bean
    @Primary
    fun adapterTransactionManager(
        @Qualifier("adapterEntityManagerFactory") entityManagerFactory: EntityManagerFactory,
    ): PlatformTransactionManager = JpaTransactionManager(entityManagerFactory)
}
