// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import io.github.oshai.kotlinlogging.KotlinLogging
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.CommandMapperFactory.Companion.GET_CONFIGURATION_REQUEST
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.util.HeaderUtil.buildResponseHeader
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetConfigurationRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.RelayMap
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.RelayType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.astronomicalOffsetsConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.communicationConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.configuration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.daylightSavingsTimeConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.deviceAddressConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.platformAddressConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayLinking
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayMap
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayMapping
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.getConfigurationResponse
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.stereotype.Component
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LightType as InternalLightType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LinkType as InternalLinkType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result as InternalResult

@Component(value = GET_CONFIGURATION_REQUEST)
class GetConfigurationCommandMapper : CommandMapper {
    private val logger = KotlinLogging.logger { }

    override fun toInternal(requestMessage: DeviceRequestMessage): DeviceRequest =
        GetConfigurationRequest(
            Device(
                requestMessage.header.deviceIdentification,
                requestMessage.header.networkAddress,
            ),
            Organization(requestMessage.header.organizationIdentification),
        )

    override fun toResponse(
        requestHeader: RequestHeader,
        envelope: Envelope,
    ): DeviceResponseMessage =
        deviceResponseMessage {
            header = buildResponseHeader(requestHeader)
            result =
                when (envelope.message.getConfigurationResponse.status) {
                    Oslp.Status.OK -> InternalResult.OK
                    else -> InternalResult.NOT_OK
                }
            getConfigurationResponse = getBody(envelope)
        }

    private fun getBody(envelope: Envelope) =
        getConfigurationResponse {
            val response = envelope.message.getConfigurationResponse
            configuration =
                configuration {
                    relayConfiguration =
                        relayConfiguration {
                            relayMapping =
                                relayMapping {
                                    relayMap.addAll(response.relayConfiguration.toInternal())
                                }
                            relayLinking =
                                relayLinking {
                                }
                            relayRefreshingEnabled = response.relayRefreshing
                        }

                    deviceAddressConfiguration =
                        deviceAddressConfiguration {
                            ipAddress = response.deviceFixIpValue
                            netMask = response.netMask
                            gateway = response.gateWay
                            dhcpEnabled = response.isDhcpEnabled
                        }

                    platformAddressConfiguration =
                        platformAddressConfiguration {
                            ipAddress = response.ospgIpAddress
                            portNumber = response.osgpPortNumber
                        }

                    communicationConfiguration =
                        communicationConfiguration {
                            response.preferredLinkType.toInternal()?.let { preferredLinkType = it }
                            connectionTimeout = response.communicationTimeout
                            numberOfRetries = response.communicationNumberOfRetries
                            delayBetweenConnectionAttempts = response.communicationPauseTimeBetweenConnectionTrials
                        }

                    daylightSavingsTimeConfiguration =
                        daylightSavingsTimeConfiguration {
                            automaticSummerTimingEnabled = response.isAutomaticSummerTimingEnabled
                            summerTimeDetails = response.summerTimeDetails
                            winterTimeDetails = response.winterTimeDetails
                        }

                    astronomicalOffsetsConfiguration =
                        astronomicalOffsetsConfiguration {
                            sunriseOffset = response.astroGateSunRiseOffset
                            sunsetOffset = response.astroGateSunSetOffset
                        }

                    response.lightType.toInternal()?.let { lightType = it }
                    testButtonEnabled = response.isTestButtonEnabled
                    timeSyncFrequency = response.timeSyncFrequency
                    switchingDelay.addAll(response.switchingDelayList)
                }
        }

    private fun Oslp.LightType.toInternal(): InternalLightType? =
        when (this) {
            Oslp.LightType.RELAY -> InternalLightType.RELAY
            else -> {
                logger.warn { "Unsupported light type: $this" }
                null
            }
        }

    private fun Oslp.LinkType.toInternal(): InternalLinkType? =
        when (this) {
            Oslp.LinkType.GPRS -> InternalLinkType.GPRS
            Oslp.LinkType.CDMA -> InternalLinkType.CDMA
            Oslp.LinkType.ETHERNET -> InternalLinkType.ETHERNET
            Oslp.LinkType.LINK_NOT_SET -> InternalLinkType.LINK_TYPE_NOT_SET
            else -> {
                logger.warn { "Unsupported link type: $this" }
                null
            }
        }

    private fun Oslp.RelayConfiguration.toInternal(): List<RelayMap> =
        addressMapList.map { am ->
            relayMap {
                index = am.index
                address = am.address
                am.relayType.toInternal()?.let { relayType = it }
            }
        }

    private fun Oslp.RelayType.toInternal(): RelayType? =
        when (this) {
            Oslp.RelayType.LIGHT -> RelayType.LIGHT
            Oslp.RelayType.RT_NOT_SET -> RelayType.RELAY_TYPE_NOT_SET
            else -> {
                logger.warn { "Unsupported relay type $this" }
                null
            }
        }
}
