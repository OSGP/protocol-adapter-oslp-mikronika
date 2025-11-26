// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.mapper.response

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.responseHeader

abstract class DeviceResponseMapper {
    abstract fun toResponse(
        requestHeader: RequestHeader,
        envelope: Envelope,
    ): DeviceResponseMessage

    protected fun buildResponseHeader(req: RequestHeader): ResponseHeader =
        responseHeader {
            correlationUid = req.correlationUid
            deviceIdentification = req.deviceIdentification
            deviceType = req.deviceType
            organizationIdentification = req.organizationIdentification
        }
}
