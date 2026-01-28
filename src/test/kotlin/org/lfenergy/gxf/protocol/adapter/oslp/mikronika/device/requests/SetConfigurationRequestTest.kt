// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.ByteString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NETWORK_ADDRESS
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.ORGANIZATION_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LightType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.LinkType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.RelayType
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.astronomicalOffsetsConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.communicationConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.configuration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.daylightSavingsTimeConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.deviceAddressConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.platformAddressConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayConfiguration
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayLinkMatrix
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayLinking
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayMap
import org.lfenergy.gxf.publiclighting.contracts.internal.configuration.relayMapping
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.setConfigurationRequest
import org.opensmartgridplatform.oslp.Oslp

class SetConfigurationRequestTest {
    @Test
    fun `should map correctly`() {
        val subject =
            SetConfigurationRequest(
                Device(DEVICE_IDENTIFICATION, NETWORK_ADDRESS),
                Organization(ORGANIZATION_IDENTIFICATION),
                setConfigurationRequest =
                    setConfigurationRequest {
                        configuration =
                            configuration {
                                astronomicalOffsetsConfiguration =
                                    astronomicalOffsetsConfiguration {
                                        sunsetOffset = SUNSET_OFFSET
                                        sunriseOffset = SUNRISE_OFFSET
                                    }
                                communicationConfiguration =
                                    communicationConfiguration {
                                        delayBetweenConnectionAttempts = DELAY_BETWEEN_CONNECTION_ATTEMPTS
                                        numberOfRetries = NUMBER_OF_RETRIES
                                        connectionTimeout = CONNECTION_TIMEOUT
                                        preferredLinkType = LinkType.valueOf(PREFERRED_LINK_TYPE)
                                    }
                                daylightSavingsTimeConfiguration =
                                    daylightSavingsTimeConfiguration {
                                        automaticSummerTimingEnabled = true
                                        summerTimeDetails = BEGIN_OF_DAYLIGHT_SAVINGS_TIME
                                        winterTimeDetails = END_OF_DAYLIGHT_SAVINGS_TIME
                                    }
                                deviceAddressConfiguration =
                                    deviceAddressConfiguration {
                                        ipAddress = DEVICE_IP_ADDRESS
                                        netMask = DEVICE_NET_MASK
                                        gateway = DEVICE_GATEWAY
                                        dhcpEnabled = true
                                    }
                                platformAddressConfiguration =
                                    platformAddressConfiguration {
                                        ipAddress = PLATFORM_IP_ADDRESS
                                        portNumber = PLATFORM_PORT_NUMBER
                                    }
                                relayConfiguration =
                                    relayConfiguration {
                                        relayLinking = relayLinking { relayLinkMatrix.addAll(RELAY_LINKS) }
                                        relayMapping = relayMapping { relayMap.addAll(RELAY_MAPS) }
                                        relayRefreshingEnabled = true
                                    }
                                lightType = LightType.valueOf(LIGHT_TYPE)
                                testButtonEnabled = true
                                timeSyncFrequency = TIME_SYNC_FREQUENCY
                                switchingDelay.addAll(SWITCHING_DELAYS)
                            }
                    },
            )

        val result = subject.toOslpMessage()

        assertThat(result.hasSetConfigurationRequest()).isTrue
        with(result.setConfigurationRequest) {
            assertThat(astroGateSunSetOffset).isEqualTo(SUNSET_OFFSET)
            assertThat(astroGateSunRiseOffset).isEqualTo(SUNRISE_OFFSET)

            assertThat(communicationPauseTimeBetweenConnectionTrials).isEqualTo(DELAY_BETWEEN_CONNECTION_ATTEMPTS)
            assertThat(communicationTimeout).isEqualTo(CONNECTION_TIMEOUT)
            assertThat(communicationNumberOfRetries).isEqualTo(NUMBER_OF_RETRIES)
            assertThat(preferredLinkType).isEqualTo(Oslp.LinkType.valueOf(PREFERRED_LINK_TYPE))

            assertThat(isAutomaticSummerTimingEnabled).isTrue
            assertThat(summerTimeDetails).isEqualTo(BEGIN_OF_DAYLIGHT_SAVINGS_TIME)
            assertThat(winterTimeDetails).isEqualTo(END_OF_DAYLIGHT_SAVINGS_TIME)

            assertThat(deviceFixIpValue).isEqualTo(DEVICE_IP_ADDRESS)
            assertThat(netMask).isEqualTo(DEVICE_NET_MASK)
            assertThat(gateWay).isEqualTo(DEVICE_GATEWAY)
            assertThat(isDhcpEnabled).isTrue

            assertThat(ospgIpAddress).isEqualTo(PLATFORM_IP_ADDRESS)
            assertThat(osgpPortNumber).isEqualTo(PLATFORM_PORT_NUMBER)

            assertThat(relayConfiguration.addressMapCount).isEqualTo(RELAY_MAPS.size)
            assertThat(relayLinkingCount).isEqualTo(RELAY_LINKS.size)
            assertThat(relayRefreshing).isTrue

            assertThat(lightType).isEqualTo(Oslp.LightType.valueOf(LIGHT_TYPE))
            assertThat(isTestButtonEnabled).isTrue
            assertThat(timeSyncFrequency).isEqualTo(TIME_SYNC_FREQUENCY)
            assertThat(switchingDelayCount).isEqualTo(SWITCHING_DELAYS.size)
        }
    }

    @Test
    fun `should map correctly when having only astronomical offsets`() {
        val subject =
            SetConfigurationRequest(
                Device(DEVICE_IDENTIFICATION, NETWORK_ADDRESS),
                Organization(ORGANIZATION_IDENTIFICATION),
                setConfigurationRequest =
                    setConfigurationRequest {
                        configuration =
                            configuration {
                                astronomicalOffsetsConfiguration =
                                    astronomicalOffsetsConfiguration {
                                        sunsetOffset = SUNSET_OFFSET
                                        sunriseOffset = SUNRISE_OFFSET
                                    }
                            }
                    },
            )

        val result = subject.toOslpMessage()

        assertThat(result.hasSetConfigurationRequest()).isTrue
        with(result.setConfigurationRequest) {
            assertThat(hasAstroGateSunSetOffset()).isTrue
            assertThat(astroGateSunSetOffset).isEqualTo(SUNSET_OFFSET)

            assertThat(hasAstroGateSunRiseOffset()).isTrue
            assertThat(astroGateSunRiseOffset).isEqualTo(SUNRISE_OFFSET)

            assertThat(hasCommunicationTimeout()).isFalse
            assertThat(hasCommunicationPauseTimeBetweenConnectionTrials()).isFalse
            assertThat(hasCommunicationNumberOfRetries()).isFalse
            assertThat(hasPreferredLinkType()).isFalse

            assertThat(hasDeviceFixIpValue()).isFalse
            assertThat(hasNetMask()).isFalse
            assertThat(hasGateWay()).isFalse
            assertThat(hasIsDhcpEnabled()).isFalse

            assertThat(hasOspgIpAddress()).isFalse
            assertThat(hasOsgpPortNumber()).isFalse

            assertThat(hasRelayConfiguration()).isFalse
            assertThat(relayLinkingCount).isEqualTo(0)
            assertThat(hasRelayRefreshing()).isFalse

            assertThat(hasLightType()).isFalse
            assertThat(hasIsTestButtonEnabled()).isFalse
            assertThat(hasTimeSyncFrequency()).isFalse
            assertThat(switchingDelayCount).isEqualTo(0)
        }
    }

    companion object {
        const val SUNSET_OFFSET = -15
        const val SUNRISE_OFFSET = 15

        const val BEGIN_OF_DAYLIGHT_SAVINGS_TIME = "0360100" // Last sunday of March at 01:00 UTC
        const val END_OF_DAYLIGHT_SAVINGS_TIME = "1060100" // Last sunday of October at 01:00 UTC

        const val DELAY_BETWEEN_CONNECTION_ATTEMPTS = 1000
        const val NUMBER_OF_RETRIES = 3
        const val CONNECTION_TIMEOUT = 2000
        const val PREFERRED_LINK_TYPE = "ETHERNET"

        val DEVICE_IP_ADDRESS: ByteString = ByteString.copyFrom(byteArrayOf(127, 0, 0, 2))
        val DEVICE_NET_MASK: ByteString = ByteString.copyFrom(byteArrayOf(255.toByte(), 255.toByte(), 255.toByte(), 0))
        val DEVICE_GATEWAY: ByteString = ByteString.copyFrom(byteArrayOf(127, 0, 0, 1))

        val PLATFORM_IP_ADDRESS: ByteString = ByteString.copyFrom(byteArrayOf(128.toByte(), 0, 0, 1))
        const val PLATFORM_PORT_NUMBER = 22125

        val RELAY_LINKS =
            listOf(
                relayLinkMatrix {
                    masterRelayIndex = ByteString.copyFrom(byteArrayOf(1))
                    masterRelayOn = true
                    indicesOfControlledRelaysOn = ByteString.copyFrom(byteArrayOf(2, 3))
                },
                relayLinkMatrix {
                    masterRelayIndex = ByteString.copyFrom(byteArrayOf(1))
                    masterRelayOn = false
                    indicesOfControlledRelaysOff = ByteString.copyFrom(byteArrayOf(2, 3))
                },
            )

        val RELAY_MAPS =
            listOf(
                relayMap {
                    index = ByteString.copyFrom(byteArrayOf(2))
                    address = ByteString.copyFrom(byteArrayOf(1))
                    relayType = RelayType.LIGHT
                },
                relayMap {
                    index = ByteString.copyFrom(byteArrayOf(3))
                    address = ByteString.copyFrom(byteArrayOf(2))
                    relayType = RelayType.LIGHT
                },
                relayMap {
                    index = ByteString.copyFrom(byteArrayOf(4))
                    address = ByteString.copyFrom(byteArrayOf(3))
                    relayType = RelayType.LIGHT
                },
            )

        const val LIGHT_TYPE = "RELAY"
        const val TIME_SYNC_FREQUENCY = 60
        val SWITCHING_DELAYS = listOf(100, 200, 300, 400)
    }
}
