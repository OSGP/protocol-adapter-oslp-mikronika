// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.AstronomicalOffsetsConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.CommunicationConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.DaylightSavingsTimeConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.DeviceAddressConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LightType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LinkType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.PlatformAddressConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.RelayConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.RelayLinkMatrix
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.RelayMap
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.SetConfigurationRequestKt
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
    override fun toOslpMessage(): Oslp.Message {
        val source = this.setConfigurationRequest.configuration

        return message {
            setConfigurationRequest =
                setConfigurationRequest {
                    if (source.hasAstronomicalOffsetsConfiguration()) {
                        add(source.astronomicalOffsetsConfiguration)
                    }
                    if (source.hasCommunicationConfiguration()) {
                        add(source.communicationConfiguration)
                    }
                    if (source.hasDaylightSavingsTimeConfiguration()) {
                        add(source.daylightSavingsTimeConfiguration)
                    }
                    if (source.hasDeviceAddressConfiguration()) {
                        add(source.deviceAddressConfiguration)
                    }
                    if (source.hasPlatformAddressConfiguration()) {
                        add(source.platformAddressConfiguration)
                    }
                    if (source.hasRelayConfiguration()) {
                        add(source.relayConfiguration)
                    }
                    if (source.hasLightType()) lightType = source.lightType.toOslp()
                    if (source.hasTestButtonEnabled()) isTestButtonEnabled = source.testButtonEnabled
                    if (source.hasTimeSyncFrequency()) timeSyncFrequency = source.timeSyncFrequency
                    if (source.switchingDelayCount > 0) switchingDelay.addAll(source.switchingDelayList)
                }
        }
    }

    private fun SetConfigurationRequestKt.Dsl.add(source: AstronomicalOffsetsConfiguration) {
        if (source.hasSunsetOffset()) astroGateSunSetOffset = source.sunsetOffset
        if (source.hasSunriseOffset()) astroGateSunRiseOffset = source.sunriseOffset
    }

    private fun SetConfigurationRequestKt.Dsl.add(source: CommunicationConfiguration) {
        if (source.hasPreferredLinkType()) preferredLinkType = source.preferredLinkType.toOslp()
        if (source.hasConnectionTimeout()) communicationTimeout = source.connectionTimeout
        if (source.hasNumberOfRetries()) communicationNumberOfRetries = source.numberOfRetries
        if (source.hasDelayBetweenConnectionAttempts()) {
            communicationPauseTimeBetweenConnectionTrials = source.delayBetweenConnectionAttempts
        }
    }

    private fun SetConfigurationRequestKt.Dsl.add(source: DaylightSavingsTimeConfiguration) {
        if (source.hasAutomaticSummerTimingEnabled()) {
            isAutomaticSummerTimingEnabled =
                source.automaticSummerTimingEnabled
        }
        if (source.hasSummerTimeDetails()) summerTimeDetails = source.summerTimeDetails
        if (source.hasWinterTimeDetails()) winterTimeDetails = source.winterTimeDetails
    }

    private fun SetConfigurationRequestKt.Dsl.add(source: DeviceAddressConfiguration) {
        if (source.hasIpAddress()) deviceFixIpValue = source.ipAddress
        if (source.hasNetMask()) netMask = source.netMask
        if (source.hasGateway()) gateWay = source.gateway
        if (source.hasDhcpEnabled()) isDhcpEnabled = source.dhcpEnabled
    }

    private fun SetConfigurationRequestKt.Dsl.add(source: PlatformAddressConfiguration) {
        if (source.hasIpAddress()) ospgIpAddress = source.ipAddress
        if (source.hasPortNumber()) osgpPortNumber = source.portNumber
    }

    private fun SetConfigurationRequestKt.Dsl.add(source: RelayConfiguration) {
        if (source.hasRelayLinking()) {
            relayLinking.addAll(source.relayLinking.relayLinkMatrixList.map { it.toOslp() })
        }
        if (source.hasRelayMapping()) {
            relayConfiguration =
                relayConfiguration {
                    addressMap.addAll(
                        source.relayMapping.relayMapList.map { it.toOslp() },
                    )
                }
        }

        if (source.hasRelayRefreshingEnabled()) relayRefreshing = source.relayRefreshingEnabled
    }

    private fun RelayLinkMatrix.toOslp(): Oslp.RelayMatrix {
        val internal = this
        return relayMatrix {
            if (internal.hasMasterRelayIndex()) masterRelayIndex = internal.masterRelayIndex
            if (internal.hasMasterRelayOn()) masterRelayOn = internal.masterRelayOn
            if (internal.hasIndicesOfControlledRelaysOn()) {
                indicesOfControlledRelaysOn =
                    internal.indicesOfControlledRelaysOn
            }
            if (internal.hasIndicesOfControlledRelaysOff()) {
                indicesOfControlledRelaysOff =
                    internal.indicesOfControlledRelaysOff
            }
        }
    }

    private fun RelayMap.toOslp(): Oslp.IndexAddressMap {
        val internal = this
        return indexAddressMap {
            if (internal.hasIndex()) index = internal.index
            if (internal.hasAddress()) address = internal.address
            if (internal.hasRelayType()) relayType = internal.relayType.toOslp()
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
