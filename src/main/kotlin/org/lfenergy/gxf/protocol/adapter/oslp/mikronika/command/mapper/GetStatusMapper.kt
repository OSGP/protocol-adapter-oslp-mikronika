// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetStatusRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.DeviceReqResMapperFactory.Companion.GET_STATUS_REQUEST
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayIndex
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.getStatusResponse
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.lightValue
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.stereotype.Component
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LightType as InternalLightType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LightValue as InternalLightValue
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LinkType as InternalLinkType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result as InternalResult

@Component(value = GET_STATUS_REQUEST)
class GetStatusMapper : DeviceReqResMapper() {
    override fun toInternal(requestMessage: DeviceRequestMessage): DeviceRequest {
        val deviceIdentification = requestMessage.header.deviceIdentification
        val networkAddress = requestMessage.header.networkAddress

        return GetStatusRequest(
            deviceIdentification,
            networkAddress,
        )
    }

    override fun toResponse(
        requestHeader: RequestHeader,
        envelope: Envelope,
    ): DeviceResponseMessage =
        deviceResponseMessage {
            header = buildResponseHeader(requestHeader)
            getStatusResponse = getBody(envelope)
        }

    private fun getBody(envelope: Envelope) =
        getStatusResponse {
            val response = envelope.message.getStatusResponse

            result =
                when (response.status) {
                    Oslp.Status.OK -> InternalResult.OK
                    else -> InternalResult.NOT_OK
                }

            lightValues += response.valueList.map { it.toInternal() }

            preferredLinkType = response.preferredLinktype.toInternal()

            actualLinkType = response.actualLinktype.toInternal()

            lightType =
                when (response.lightType) {
                    Oslp.LightType.RELAY -> InternalLightType.RELAY
                    Oslp.LightType.ONE_TO_TEN_VOLT -> InternalLightType.ONE_TO_TEN_VOLT
                    Oslp.LightType.ONE_TO_TEN_VOLT_REVERSE -> InternalLightType.ONE_TO_TEN_VOLT_REVERSE
                    Oslp.LightType.DALI -> InternalLightType.DALI
                    else -> InternalLightType.RELAY
                }
            eventNotificationMask = eventNotificationMask
            numberOfOutputs = numberOfOutputs
            dcOutputVoltageMaximum = dcOutputVoltageMaximum
            dcOutputVoltageCurrent = dcOutputVoltageCurrent
            maximumOutputPowerOnDcOutput = maximumOutputPowerOnDcOutput
            serialNumber = serialNumber
            macAddress = macAddress
            hardwareId = hardwareId
            internalFlashMemSize = internalFlashMemSize
            externalFlashMemSize = externalFlashMemSize
            lastInternalTestResultCode = lastInternalTestResultCode
            startupCounter = startupCounter
            bootLoaderVersion = bootLoaderVersion
            firmwareVersion = firmwareVersion
            currentConfigurationBackUsed = currentConfigurationBackUsed
            name = name
            currentTime = currentTime
            currentIp = currentIp
        }

    private fun Oslp.LinkType.toInternal(): InternalLinkType =
        when (this) {
            Oslp.LinkType.GPRS -> InternalLinkType.GPRS
            Oslp.LinkType.CDMA -> InternalLinkType.CDMA
            Oslp.LinkType.ETHERNET -> InternalLinkType.ETHERNET
            else -> InternalLinkType.GPRS // Default?
        }

    private fun Oslp.LightValue.toInternal(): InternalLightValue =
        lightValue {
            index =
                when (index.number) {
                    0 -> RelayIndex.RELAY_ALL
                    1 -> RelayIndex.RELAY_ONE
                    2 -> RelayIndex.RELAY_TWO
                    3 -> RelayIndex.RELAY_THREE
                    4 -> RelayIndex.RELAY_FOUR
                    else -> RelayIndex.RELAY_ALL
                }
            lightOn = on
        }
}
