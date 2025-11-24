package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "device-commands")
class DeviceRequestConfigurationProperties(
    var producer: ProducerProperties,
) {
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
