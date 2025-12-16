// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.GET_STATUS_REQUEST
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.util.HeaderUtil.buildResponseHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetStatusRequest
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
class GetStatusCommandMapper : CommandMapper() {
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
            result =
                when (envelope.message.getStatusResponse.status) {
                    Oslp.Status.OK -> InternalResult.OK
                    else -> InternalResult.NOT_OK
                }
            getStatusResponse = getBody(envelope)
        }

    private fun getBody(envelope: Envelope) =
        getStatusResponse {
            val response = envelope.message.getStatusResponse

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
            eventNotificationMask = response.eventNotificationMask
            numberOfOutputs = response.numberOfOutputs
            dcOutputVoltageMaximum = response.dcOutputVoltageMaximum
            dcOutputVoltageCurrent = response.dcOutputVoltageCurrent
            maximumOutputPowerOnDcOutput = response.maximumOutputPowerOnDcOutput
            serialNumber = response.serialNumber
            macAddress = response.macAddress
            hardwareId = response.hardwareId
            internalFlashMemSize = response.internalFlashMemSize
            externalFlashMemSize = response.externalFlashMemSize
            lastInternalTestResultCode = response.lastInternalTestResultCode
            startupCounter = response.startupCounter
            bootLoaderVersion = response.bootLoaderVersion
            firmwareVersion = response.firmwareVersion
            currentConfigurationBackUsed = response.currentConfigurationBackUsed
            name = response.name
            currentTime = response.currentTime
            currentIp = response.currentIp
        }

    private fun Oslp.LinkType.toInternal(): InternalLinkType =
        when (this) {
            Oslp.LinkType.GPRS -> InternalLinkType.GPRS
            Oslp.LinkType.CDMA -> InternalLinkType.CDMA
            Oslp.LinkType.ETHERNET -> InternalLinkType.ETHERNET
            else -> throw IllegalArgumentException("Unknown LinkType: $this")
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
