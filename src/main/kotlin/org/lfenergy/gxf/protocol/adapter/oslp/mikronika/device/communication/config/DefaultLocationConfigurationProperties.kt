// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("default-location")
class DefaultLocationConfigurationProperties(
    var latitude: Float = 52.132633f,
    var longitude: Float = 5.291266f,
)
