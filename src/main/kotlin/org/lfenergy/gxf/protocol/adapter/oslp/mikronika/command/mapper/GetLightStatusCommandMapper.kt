// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.GET_LIGHT_STATUS_REQUEST
import org.springframework.stereotype.Component

@Component(value = GET_LIGHT_STATUS_REQUEST)
class GetLightStatusCommandMapper : GetStatusCommandMapper()
