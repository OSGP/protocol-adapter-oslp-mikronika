// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.deviceeventpublisher.config.DeviceEventsConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

@Configuration
@EnableAsync
@EnableConfigurationProperties(DeviceEventsConfigurationProperties::class)
class DeviceEventsModuleConfiguration
