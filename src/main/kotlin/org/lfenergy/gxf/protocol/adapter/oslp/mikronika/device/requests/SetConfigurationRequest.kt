// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LightType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LinkType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.GetConfigurationResponse
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.indexAddressMap
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.relayConfiguration
import org.opensmartgridplatform.oslp.relayMatrix
import org.opensmartgridplatform.oslp.setConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.RelayType as InternalRelayType

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
                    val resultConfig = getConfigurationResult.configuration

                    relayLinking.addAll(
                        resultConfig.relayConfiguration.relayLinking.relayLinkMatrixList.map {
                            relayMatrix {
                                masterRelayIndex = it.masterRelayIndex
                                masterRelayOn = it.masterRelayOn
                                indicesOfControlledRelaysOn = it.indicesOfControlledRelaysOn
                                indicesOfControlledRelaysOff = it.indicesOfControlledRelaysOff
                            }
                        },
                    )

                    relayConfiguration =
                        relayConfiguration {
                            addressMap.addAll(
                                resultConfig.relayConfiguration.relayMapping.relayMapList.map {
                                    indexAddressMap {
                                        index = it.index
                                        address = it.address
                                        relayType = it.relayType.toOslp()
                                    }
                                },
                            )
                        }

                    relayRefreshing = resultConfig.relayConfiguration.relayRefreshingEnabled

                    deviceFixIpValue = resultConfig.deviceAddressConfiguration.ipAddress
                    netMask = resultConfig.deviceAddressConfiguration.netMask
                    gateWay = resultConfig.deviceAddressConfiguration.gateway
                    isDhcpEnabled = resultConfig.deviceAddressConfiguration.dhcpEnabled

                    ospgIpAddress = resultConfig.platformAddressConfiguration.ipAddress
                    osgpPortNumber = resultConfig.platformAddressConfiguration.portNumber

                    preferredLinkType = resultConfig.communicationConfiguration.preferredLinkType.toOslp()
                    communicationTimeout = resultConfig.communicationConfiguration.connectionTimeout
                    communicationNumberOfRetries = resultConfig.communicationConfiguration.numberOfRetries
                    communicationPauseTimeBetweenConnectionTrials =
                        resultConfig.communicationConfiguration.numberOfRetries

                    isAutomaticSummerTimingEnabled =
                        resultConfig.daylightSavingsTimeConfiguration.automaticSummerTimingEnabled
                    summerTimeDetails = resultConfig.daylightSavingsTimeConfiguration.summerTimeDetails
                    winterTimeDetails = resultConfig.daylightSavingsTimeConfiguration.winterTimeDetails

                    astroGateSunRiseOffset = resultConfig.astronomicalOffsetsConfiguration.sunriseOffset
                    astroGateSunSetOffset = resultConfig.astronomicalOffsetsConfiguration.sunsetOffset

                    lightType = resultConfig.lightType.toOslp()
                    isTestButtonEnabled = resultConfig.testButtonEnabled
                    timeSyncFrequency = resultConfig.timeSyncFrequency
                    switchingDelay.addAll(resultConfig.switchingDelayList)
                }
        }

    private fun LinkType.toOslp(): Oslp.LinkType =
        when (this) {
            LinkType.LINK_TYPE_NOT_SET -> Oslp.LinkType.LINK_NOT_SET
            LinkType.GPRS -> Oslp.LinkType.GPRS
            LinkType.CDMA -> Oslp.LinkType.CDMA
            LinkType.ETHERNET -> Oslp.LinkType.ETHERNET
            else -> Oslp.LinkType.LINK_NOT_SET
        }

    private fun LightType.toOslp(): Oslp.LightType =
        when (this) {
            LightType.RELAY -> Oslp.LightType.RELAY
            LightType.ONE_TO_TEN_VOLT -> Oslp.LightType.ONE_TO_TEN_VOLT
            LightType.ONE_TO_TEN_VOLT_REVERSE -> Oslp.LightType.ONE_TO_TEN_VOLT_REVERSE
            LightType.DALI -> Oslp.LightType.DALI
            else -> Oslp.LightType.RELAY
        }

    private fun InternalRelayType.toOslp(): Oslp.RelayType =
        when (this) {
            InternalRelayType.RELAY_TYPE_NOT_SET -> Oslp.RelayType.RT_NOT_SET
            InternalRelayType.LIGHT -> Oslp.RelayType.LIGHT
            InternalRelayType.TARIFF -> Oslp.RelayType.TARIFF
            else -> Oslp.RelayType.RT_NOT_SET
        }
}
