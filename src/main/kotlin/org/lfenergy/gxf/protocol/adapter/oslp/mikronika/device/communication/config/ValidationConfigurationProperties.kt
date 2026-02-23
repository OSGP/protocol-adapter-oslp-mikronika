// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("validation")
class ValidationConfigurationProperties {
    var sequenceNumber: SequenceNumber = SequenceNumber()

    class SequenceNumber {
        var max: Int = 65535
        var window: Int = 6
    }
}
