// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration::class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration::class,
        org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration::class,
    ],
)
class ProtocolAdapterOslpMikronikaApplication

fun main(args: Array<String>) {
    runApplication<ProtocolAdapterOslpMikronikaApplication>(*args)
}
