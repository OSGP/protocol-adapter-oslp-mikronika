// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.service

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.util.HeaderUtil.buildResponseHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.DeviceClientService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.errorResponse

sealed class RequestService(
    protected val deviceClientService: DeviceClientService,
) {
    protected fun sendDeviceRequest(
        deviceRequest: DeviceRequest,
        onSuccess: (Envelope) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        deviceClientService.sendClientMessage(deviceRequest) { result ->
            result
                .onSuccess { responseEnvelope -> onSuccess(responseEnvelope) }
                .onFailure { exception -> onFailure(exception) }
        }
    }

    protected fun createErrorMessage(
        requestHeader: RequestHeader,
        exception: Throwable,
    ): DeviceResponseMessage =
        deviceResponseMessage {
            header = buildResponseHeader(requestHeader)
            result = Result.NOT_OK
            errorResponse {
                errorMessage = exception.message ?: "Unknown exception"
            }
        }
}
