// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.setTransitionRequest

class SetTransitionRequest(
    device: Device,
    organization: Organization,
    val transitionType: TransitionType,
    val time: String,
) : DeviceRequest(
        device,
        organization,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setTransitionRequest =
                setTransitionRequest {
                    time = this@SetTransitionRequest.time
                    transitionType =
                        when (this@SetTransitionRequest.transitionType) {
                            TransitionType.DAY_NIGHT -> Oslp.TransitionType.DAY_NIGHT
                            TransitionType.NIGHT_DAY -> Oslp.TransitionType.NIGHT_DAY
                        }
                }
        }

    enum class TransitionType {
        NIGHT_DAY,
        DAY_NIGHT,
    }
}
