// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.LightValue
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.lightValue
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setLightRequest
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message

class SetLightRequest(
    deviceIdentification: String,
    networkAddress: String,
    val lightValues: List<LightValue>,
) : DeviceRequest(
        deviceIdentification,
        networkAddress,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setLightRequest {
                this@SetLightRequest.lightValues.forEach {
                    lightValue {
                        index = it.index
                        lightOn = it.lightOn
                    }
                }
            }
        }
}
