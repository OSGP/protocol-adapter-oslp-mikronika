// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.kotlin.toByteStringUtf8
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.LightValue
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.lightValue
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setLightRequest

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
            setLightRequest =
                setLightRequest {
                    values.addAll(
                        this@SetLightRequest.lightValues.map {
                            lightValue {
                                index =
                                    it.index.number
                                        .toString()
                                        .toByteStringUtf8()
                                on = it.lightOn
                            }
                        },
                    )
                }
        }
}
