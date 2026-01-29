// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.Configuration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LightType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LinkType
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
        val requestedConfiguration = this.setConfigurationRequest.configuration

        return message {
            setConfigurationRequest =
                setConfigurationRequest {
                    addAstronomicalOffsetConfiguration(this, requestedConfiguration)
                    addCommunicationConfiguration(this, requestedConfiguration)
                    addDaylightSavingsConfiguration(this, requestedConfiguration)
                    addDeviceAddressConfiguration(this, requestedConfiguration)
                    addPlatformAddressConfiguration(this, requestedConfiguration)
                    addRelayConfiguration(this, requestedConfiguration)
                    addOtherFields(this, requestedConfiguration)
                }
        }
    }

    private fun addAstronomicalOffsetConfiguration(
        dsl: SetConfigurationRequestKt.Dsl,
        requestedConfiguration: Configuration,
    ) {
        if (requestedConfiguration.hasAstronomicalOffsetsConfiguration()) {
            with(requestedConfiguration.astronomicalOffsetsConfiguration) {
                if (hasSunsetOffset()) dsl.astroGateSunSetOffset = sunsetOffset
                if (hasSunriseOffset()) dsl.astroGateSunRiseOffset = sunriseOffset
            }
        }
    }

    private fun addCommunicationConfiguration(
        dsl: SetConfigurationRequestKt.Dsl,
        requestedConfiguration: Configuration,
    ) {
        if (requestedConfiguration.hasCommunicationConfiguration()) {
            with(requestedConfiguration.communicationConfiguration) {
                if (hasPreferredLinkType()) dsl.preferredLinkType = preferredLinkType.toOslp()
                if (hasConnectionTimeout()) dsl.communicationTimeout = connectionTimeout
                if (hasNumberOfRetries()) dsl.communicationNumberOfRetries = numberOfRetries
                if (hasDelayBetweenConnectionAttempts()) {
                    dsl.communicationPauseTimeBetweenConnectionTrials =
                        delayBetweenConnectionAttempts
                }
            }
        }
    }

    private fun addDaylightSavingsConfiguration(
        dsl: SetConfigurationRequestKt.Dsl,
        requestedConfiguration: Configuration,
    ) {
        if (requestedConfiguration.hasDaylightSavingsTimeConfiguration()) {
            with(requestedConfiguration.daylightSavingsTimeConfiguration) {
                if (hasAutomaticSummerTimingEnabled()) dsl.isAutomaticSummerTimingEnabled = automaticSummerTimingEnabled
                if (hasSummerTimeDetails()) dsl.summerTimeDetails = summerTimeDetails
                if (hasWinterTimeDetails()) dsl.winterTimeDetails = winterTimeDetails
            }
        }
    }

    private fun addDeviceAddressConfiguration(
        dsl: SetConfigurationRequestKt.Dsl,
        requestedConfiguration: Configuration,
    ) {
        if (requestedConfiguration.hasDeviceAddressConfiguration()) {
            with(requestedConfiguration.deviceAddressConfiguration) {
                if (hasIpAddress()) dsl.deviceFixIpValue = ipAddress
                if (hasNetMask()) dsl.netMask = netMask
                if (hasGateway()) dsl.gateWay = gateway
                if (hasDhcpEnabled()) dsl.isDhcpEnabled = dhcpEnabled
            }
        }
    }

    private fun addPlatformAddressConfiguration(
        dsl: SetConfigurationRequestKt.Dsl,
        requestedConfiguration: Configuration,
    ) {
        if (requestedConfiguration.hasPlatformAddressConfiguration()) {
            with(requestedConfiguration.platformAddressConfiguration) {
                if (hasIpAddress()) dsl.ospgIpAddress = ipAddress
                if (hasPortNumber()) dsl.osgpPortNumber = portNumber
            }
        }
    }

    private fun addRelayConfiguration(
        dsl: SetConfigurationRequestKt.Dsl,
        requestedConfiguration: Configuration,
    ) {
        if (requestedConfiguration.hasRelayConfiguration()) {
            with(requestedConfiguration.relayConfiguration) {
                if (hasRelayLinking()) {
                    dsl.apply {
                        relayLinking.addAll(this@with.relayLinking.relayLinkMatrixList.map { it.toOslp() })
                    }
                }
                if (hasRelayMapping()) {
                    dsl.relayConfiguration =
                        relayConfiguration {
                            addressMap.addAll(
                                relayMapping.relayMapList.map { it.toOslp() },
                            )
                        }
                }
                if (hasRelayRefreshingEnabled()) dsl.relayRefreshing = relayRefreshingEnabled
            }
        }
    }

    private fun addOtherFields(
        dsl: SetConfigurationRequestKt.Dsl,
        requestedConfiguration: Configuration,
    ) {
        with(requestedConfiguration) {
            if (hasLightType()) dsl.lightType = lightType.toOslp()
            if (hasTestButtonEnabled()) dsl.isTestButtonEnabled = testButtonEnabled
            if (hasTimeSyncFrequency()) dsl.timeSyncFrequency = timeSyncFrequency
            if (switchingDelayCount > 0) dsl.apply { switchingDelay.addAll(switchingDelayList) }
        }
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
