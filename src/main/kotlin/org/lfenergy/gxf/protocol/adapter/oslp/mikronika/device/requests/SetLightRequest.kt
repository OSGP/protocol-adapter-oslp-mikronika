// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.kotlin.toByteString
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organisation
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.LightValue
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.lightValue
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setLightRequest

class SetLightRequest(
    device: Device,
    organisation: Organisation,
    val lightValues: List<LightValue>,
) : DeviceRequest(
        device,
        organisation,
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
                                        .toByteArray(1)
                                        .toByteString()
                                on = it.lightOn
                            }
                        },
                    )
                }
        }
}
