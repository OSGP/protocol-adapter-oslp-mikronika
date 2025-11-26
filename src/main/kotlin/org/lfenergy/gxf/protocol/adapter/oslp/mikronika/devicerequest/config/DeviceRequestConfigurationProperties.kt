// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "device-requests")
class DeviceRequestConfigurationProperties(
    var consumer: ConsumerProperties,
    var producer: ProducerProperties,
) {
    data class ConsumerProperties(
        var inboundQueue: String,
    )

    data class ProducerProperties(
        var outboundQueue: String,
        var qualityOfService: QualityOfService,
    )

    data class QualityOfService(
        var explicitQosEnabled: Boolean = false,
        var deliveryPersistent: Boolean = false,
        var priority: Int = 4,
        var timeToLive: Long = 0L,
    )
}
