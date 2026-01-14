// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LightType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LinkType
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.indexAddressMap
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.relayConfiguration
import org.opensmartgridplatform.oslp.relayMatrix
import org.opensmartgridplatform.oslp.setConfigurationRequest
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.RelayType as InternalRelayType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.SetConfigurationRequest as InternalSetConfigurationRequest

class SetConfigurationRequest(
    device: Device,
    organization: Organization,
    val setConfigurationRequest: InternalSetConfigurationRequest,
) : DeviceRequest(
        device,
        organization,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setConfigurationRequest =
                setConfigurationRequest {
                    val setConfigRequest = this@SetConfigurationRequest.setConfigurationRequest.configuration

                    relayLinking.addAll(
                        setConfigRequest.relayConfiguration.relayLinking.relayLinkMatrixList.map {
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
                                setConfigRequest.relayConfiguration.relayMapping.relayMapList.map {
                                    indexAddressMap {
                                        index = it.index
                                        address = it.address
                                        relayType = it.relayType.toOslp()
                                    }
                                },
                            )
                        }

                    relayRefreshing = setConfigRequest.relayConfiguration.relayRefreshingEnabled

                    deviceFixIpValue = setConfigRequest.deviceAddressConfiguration.ipAddress
                    netMask = setConfigRequest.deviceAddressConfiguration.netMask
                    gateWay = setConfigRequest.deviceAddressConfiguration.gateway
                    isDhcpEnabled = setConfigRequest.deviceAddressConfiguration.dhcpEnabled
                    ospgIpAddress = setConfigRequest.platformAddressConfiguration.ipAddress
                    osgpPortNumber = setConfigRequest.platformAddressConfiguration.portNumber
                    preferredLinkType = setConfigRequest.communicationConfiguration.preferredLinkType.toOslp()
                    communicationTimeout = setConfigRequest.communicationConfiguration.connectionTimeout
                    communicationNumberOfRetries = setConfigRequest.communicationConfiguration.numberOfRetries
                    communicationPauseTimeBetweenConnectionTrials =
                        setConfigRequest.communicationConfiguration.delayBetweenConnectionAttempts
                    isAutomaticSummerTimingEnabled =
                        setConfigRequest.daylightSavingsTimeConfiguration.automaticSummerTimingEnabled
                    summerTimeDetails = setConfigRequest.daylightSavingsTimeConfiguration.summerTimeDetails
                    winterTimeDetails = setConfigRequest.daylightSavingsTimeConfiguration.winterTimeDetails
                    astroGateSunRiseOffset = setConfigRequest.astronomicalOffsetsConfiguration.sunriseOffset
                    astroGateSunSetOffset = setConfigRequest.astronomicalOffsetsConfiguration.sunsetOffset

                    lightType = setConfigRequest.lightType.toOslp()
                    isTestButtonEnabled = setConfigRequest.testButtonEnabled
                    timeSyncFrequency = setConfigRequest.timeSyncFrequency
                    switchingDelay.addAll(setConfigRequest.switchingDelayList)
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
