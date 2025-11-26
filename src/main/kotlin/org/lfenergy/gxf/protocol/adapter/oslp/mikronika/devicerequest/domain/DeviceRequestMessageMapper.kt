// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.domain

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.getStatusRequest
import org.opensmartgridplatform.oslp.message

object DeviceRequestMessageMapper {
    fun DeviceRequestMessage.toGetStatusRequest(): Oslp.Message =
        message {
            getStatusRequest {}
        }
}
