// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.GET_CONFIGURATION_REQUEST
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.daliConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.getConfigurationResponse
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.indexAddressMap
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.relayConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.relayMatrix
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.stereotype.Component
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DaliConfiguration as InternalDaliConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.IndexAddressMap as InternalAddressMap
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LightType as InternalLightType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LinkType as InternalLinkType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LongTermIntervalType as InternalLongTermIntervalType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.MeterType as InternalMeterType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayType as InternalRelayType

@Component(value = GET_CONFIGURATION_REQUEST)
class GetConfigurationCommandMapper : CommandMapper() {
    override fun toInternal(requestMessage: DeviceRequestMessage): DeviceRequest {
        val deviceIdentification = requestMessage.header.deviceIdentification
        val networkAddress = requestMessage.header.networkAddress

        return GetConfigurationRequest(
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
            getConfigurationResponse = getBody(envelope)
        }

    private fun getBody(envelope: Envelope) =
        getConfigurationResponse {
            val response = envelope.message.getConfigurationResponse
            lightType = response.lightType.toInternal()
            daliConfiguration = mapOslpDaliConfigurationToInternal(response.daliConfiguration)
            relayConfiguration {
                addressMap.addAll(response.relayConfiguration.addressMapList.toInternal())
            }

            shortTermHistoryIntervalMinutes = response.shortTermHistoryIntervalMinutes
            preferredLinkType = response.preferredLinkType.toInternal()

            meterType = response.meterType.toInternal()
            longTermHistoryInterval = response.longTermHistoryInterval

            longTermHistoryIntervalType = response.longTermHistoryIntervalType.toInternal()
            timeSyncFrequency = response.timeSyncFrequency
            deviceFixIpValue = response.deviceFixIpValue
            netMask = response.netMask
            gateWay = response.gateWay
            isDhcpEnabled = response.isDhcpEnabled
            communicationTimeout = response.communicationTimeout
            communicationNumberOfRetries = response.communicationTimeout
            communicationPauseTimeBetweenConnectionTrials = response.communicationPauseTimeBetweenConnectionTrials
            osgpIpAddress = response.ospgIpAddress
            osgpPortNumber = response.osgpPortNumber
            isTestButtonEnabled = response.isTestButtonEnabled
            isAutomaticSummerTimingEnabled = response.isAutomaticSummerTimingEnabled
            astroGateSunRiseOffset = response.astroGateSunRiseOffset
            astroGateSunSetOffset = response.astroGateSunSetOffset
            switchingDelay.addAll(response.switchingDelayList)
            relayLinking.addAll(
                response.relayLinkingList.map {
                    relayMatrix {
                        masterRelayIndex = it.masterRelayIndex
                        masterRelayOn = it.masterRelayOn
                        indicesOfControlledRelaysOn = it.indicesOfControlledRelaysOn
                        indicesOfControlledRelaysOff = it.indicesOfControlledRelaysOff
                    }
                },
            )
            relayRefreshing = response.relayRefreshing
            summerTimeDetails = response.summerTimeDetails
            winterTimeDetails = response.winterTimeDetails
        }

    private fun Oslp.LightType.toInternal(): InternalLightType =
        when (this) {
            Oslp.LightType.RELAY -> InternalLightType.RELAY
            Oslp.LightType.ONE_TO_TEN_VOLT -> InternalLightType.ONE_TO_TEN_VOLT
            Oslp.LightType.ONE_TO_TEN_VOLT_REVERSE -> InternalLightType.ONE_TO_TEN_VOLT_REVERSE
            Oslp.LightType.DALI -> InternalLightType.DALI
            else -> InternalLightType.RELAY
        }

    fun mapOslpDaliConfigurationToInternal(oslpDaliConfiguration: Oslp.DaliConfiguration): InternalDaliConfiguration =
        daliConfiguration {
            numberOfLights = oslpDaliConfiguration.numberOfLights
            addressMap.addAll(oslpDaliConfiguration.addressMapList.toInternal())
        }

    private fun List<Oslp.IndexAddressMap>.toInternal(): List<InternalAddressMap> =
        this.map { oslpMap ->
            indexAddressMap {
                index = oslpMap.index
                address = oslpMap.address
                relayType = mapOslpRelayTypeToInternal(oslpMap.relayType)
            }
        }

    private fun mapOslpRelayTypeToInternal(oslpRelayType: Oslp.RelayType): InternalRelayType =
        when (oslpRelayType) {
            Oslp.RelayType.RT_NOT_SET -> InternalRelayType.RT_NOT_SET
            Oslp.RelayType.LIGHT -> InternalRelayType.LIGHT
            Oslp.RelayType.TARIFF -> InternalRelayType.TARIFF
            else -> InternalRelayType.RT_NOT_SET
        }

    private fun Oslp.LinkType.toInternal(): InternalLinkType =
        when (this) {
            Oslp.LinkType.GPRS -> InternalLinkType.GPRS
            Oslp.LinkType.CDMA -> InternalLinkType.CDMA
            Oslp.LinkType.ETHERNET -> InternalLinkType.ETHERNET
            else -> InternalLinkType.GPRS // Default?
        }

    private fun Oslp.MeterType.toInternal(): InternalMeterType =
        when (this) {
            Oslp.MeterType.MT_NOT_SET -> InternalMeterType.MT_NOT_SET
            Oslp.MeterType.P1 -> InternalMeterType.P1
            Oslp.MeterType.PULSE -> InternalMeterType.PULSE
            Oslp.MeterType.AUX -> InternalMeterType.AUX
            else -> InternalMeterType.MT_NOT_SET
        }

    private fun Oslp.LongTermIntervalType.toInternal(): InternalLongTermIntervalType =
        when (this) {
            Oslp.LongTermIntervalType.LT_INT_NOT_SET -> InternalLongTermIntervalType.LT_INT_NOT_SET
            Oslp.LongTermIntervalType.DAYS -> InternalLongTermIntervalType.DAYS
            Oslp.LongTermIntervalType.MONTHS -> InternalLongTermIntervalType.MONTHS
            else -> InternalLongTermIntervalType.LT_INT_NOT_SET
        }
}
