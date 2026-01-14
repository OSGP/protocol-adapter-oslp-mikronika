// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.kotlin.toByteString
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.resumeScheduleRequest

class ResumeScheduleRequest(
    device: Device,
    organization: Organization,
    val index: Int,
    val immediate: Boolean,
) : DeviceRequest(
        device,
        organization,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            resumeScheduleRequest =
                resumeScheduleRequest {
                    index = this@ResumeScheduleRequest.index.toByteArray(1).toByteString()
                    immediate = this@ResumeScheduleRequest.immediate
                }
        }
}
