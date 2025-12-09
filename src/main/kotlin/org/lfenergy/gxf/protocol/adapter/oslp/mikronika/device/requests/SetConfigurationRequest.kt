// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DaliConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.GetConfigurationResponse
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LightType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LinkType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LongTermIntervalType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.MeterType
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.daliConfiguration
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.relayConfiguration
import org.opensmartgridplatform.oslp.relayMatrix
import org.opensmartgridplatform.oslp.setConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.IndexAddressMap as InternalAddressMap
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayType as InternalRelayType

class SetConfigurationRequest(
    deviceIdentification: String,
    networkAddress: String,
    val getConfigurationResult: GetConfigurationResponse,
) : DeviceRequest(
        deviceIdentification,
        networkAddress,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setConfigurationRequest =
                setConfigurationRequest {
                    lightType = getConfigurationResult.lightType.toOslp()
                    daliConfiguration = getConfigurationResult.daliConfiguration.toOslp()
                    relayConfiguration =
                        relayConfiguration {
                            addressMap.addAll(getConfigurationResult.relayConfiguration.addressMapList.toOslp())
                        }
                    shortTermHistoryIntervalMinutes = getConfigurationResult.shortTermHistoryIntervalMinutes
                    preferredLinkType = getConfigurationResult.preferredLinkType.toOslp()
                    meterType = getConfigurationResult.meterType.toOslp()
                    longTermHistoryInterval = getConfigurationResult.longTermHistoryInterval
                    longTermHistoryIntervalType = getConfigurationResult.longTermHistoryIntervalType.toOslp()
                    timeSyncFrequency = getConfigurationResult.timeSyncFrequency
                    deviceFixIpValue = getConfigurationResult.deviceFixIpValue
                    netMask = getConfigurationResult.netMask
                    gateWay = getConfigurationResult.gateWay
                    isDhcpEnabled = getConfigurationResult.isDhcpEnabled
                    communicationTimeout = getConfigurationResult.communicationTimeout
                    communicationNumberOfRetries = getConfigurationResult.communicationNumberOfRetries
                    communicationPauseTimeBetweenConnectionTrials =
                        getConfigurationResult.communicationPauseTimeBetweenConnectionTrials
                    ospgIpAddress = getConfigurationResult.osgpIpAddress
                    osgpPortNumber = getConfigurationResult.osgpPortNumber
                    isTestButtonEnabled = getConfigurationResult.isTestButtonEnabled
                    isAutomaticSummerTimingEnabled = getConfigurationResult.isAutomaticSummerTimingEnabled
                    astroGateSunRiseOffset = getConfigurationResult.astroGateSunRiseOffset
                    astroGateSunSetOffset = getConfigurationResult.astroGateSunSetOffset
                    switchingDelay.addAll(getConfigurationResult.switchingDelayList)
                    relayLinking.addAll(
                        getConfigurationResult.relayLinkingList.map {
                            relayMatrix {
                                masterRelayIndex = it.masterRelayIndex
                                masterRelayOn = it.masterRelayOn
                                indicesOfControlledRelaysOn = it.indicesOfControlledRelaysOn
                                indicesOfControlledRelaysOff = it.indicesOfControlledRelaysOff
                            }
                        },
                    )
                    relayRefreshing = getConfigurationResult.relayRefreshing
                    summerTimeDetails = getConfigurationResult.summerTimeDetails
                    winterTimeDetails = getConfigurationResult.winterTimeDetails
                }
        }

    private fun LongTermIntervalType.toOslp(): Oslp.LongTermIntervalType =
        when (this) {
            LongTermIntervalType.LT_INT_NOT_SET -> Oslp.LongTermIntervalType.LT_INT_NOT_SET
            LongTermIntervalType.DAYS -> Oslp.LongTermIntervalType.DAYS
            LongTermIntervalType.MONTHS -> Oslp.LongTermIntervalType.MONTHS
            else -> Oslp.LongTermIntervalType.LT_INT_NOT_SET
        }

    private fun LinkType.toOslp(): Oslp.LinkType =
        when (this) {
            LinkType.LINK_NOT_SET -> Oslp.LinkType.LINK_NOT_SET
            LinkType.GPRS -> Oslp.LinkType.GPRS
            LinkType.CDMA -> Oslp.LinkType.CDMA
            LinkType.ETHERNET -> Oslp.LinkType.ETHERNET
            else -> Oslp.LinkType.LINK_NOT_SET
        }

    private fun MeterType.toOslp(): Oslp.MeterType =
        when (this) {
            MeterType.MT_NOT_SET -> Oslp.MeterType.MT_NOT_SET
            MeterType.P1 -> Oslp.MeterType.P1
            MeterType.PULSE -> Oslp.MeterType.PULSE
            MeterType.AUX -> Oslp.MeterType.AUX
            else -> Oslp.MeterType.MT_NOT_SET
        }

    private fun LightType.toOslp(): Oslp.LightType =
        when (this) {
            LightType.RELAY -> Oslp.LightType.RELAY
            LightType.ONE_TO_TEN_VOLT -> Oslp.LightType.ONE_TO_TEN_VOLT
            LightType.ONE_TO_TEN_VOLT_REVERSE -> Oslp.LightType.ONE_TO_TEN_VOLT_REVERSE
            LightType.DALI -> Oslp.LightType.DALI
            else -> Oslp.LightType.RELAY
        }

    private fun DaliConfiguration.toOslp(): Oslp.DaliConfiguration =
        daliConfiguration {
            numberOfLights = this@toOslp.numberOfLights
            addressMap.addAll(this@toOslp.addressMapList.toOslp())
        }

    internal fun List<InternalAddressMap>.toOslp(): List<Oslp.IndexAddressMap> =
        this.map { internalMap ->
            Oslp.IndexAddressMap
                .newBuilder()
                .setIndex(internalMap.index)
                .setAddress(internalMap.address)
                .setRelayType(internalMap.relayType.toOslp())
                .build()
        }

    private fun InternalRelayType.toOslp(): Oslp.RelayType =
        when (this) {
            InternalRelayType.RT_NOT_SET -> Oslp.RelayType.RT_NOT_SET
            InternalRelayType.LIGHT -> Oslp.RelayType.LIGHT
            InternalRelayType.TARIFF -> Oslp.RelayType.TARIFF
            else -> Oslp.RelayType.RT_NOT_SET
        }
}
