// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.kotlin.toByteStringUtf8
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.resumeScheduleRequest

class ResumeScheduleRequest(
    deviceIdentification: String,
    networkAddress: String,
    val index: String,
    val immediate: Boolean,
) : DeviceRequest(
        deviceIdentification,
        networkAddress,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            resumeScheduleRequest =
                resumeScheduleRequest {
                    index = this@ResumeScheduleRequest.index.toByteStringUtf8()
                    immediate = this@ResumeScheduleRequest.immediate
                }
        }
}
