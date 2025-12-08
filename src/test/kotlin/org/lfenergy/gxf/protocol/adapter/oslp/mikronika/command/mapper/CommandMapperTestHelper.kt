// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.requestHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import kotlin.test.assertEquals

internal const val DEVICE_IDENTIFICATION = "deviceIdentification"
internal const val FIRMWARE_VERSION = "firmwareVersion"
internal const val CORRELATION_UID = "correlationUid"
internal const val DEVICE_TYPE = "deviceType"
internal const val ORGANIZATION_IDENTIFICATION = "organizationIdentification"
internal const val DOMAIN = "domain"
internal const val DOMAIN_VERSION = "domainVersion"
internal const val NETWORK_ADDRESS = "networkAddress"

internal fun assertRequestHeader(result: DeviceResponseMessage) {
    assertEquals(DEVICE_IDENTIFICATION, result.header.deviceIdentification)
    assertEquals(CORRELATION_UID, result.header.correlationUid)
    assertEquals(DEVICE_TYPE, result.header.deviceType)
    assertEquals(ORGANIZATION_IDENTIFICATION, result.header.organizationIdentification)
    assertEquals(DOMAIN, result.header.domain)
    assertEquals(DOMAIN_VERSION, result.header.domainVersion)
    assertEquals(1, result.header.priority)
}

internal val requestHeader =
    requestHeader {
        deviceIdentification = DEVICE_IDENTIFICATION
        networkAddress = NETWORK_ADDRESS
        correlationUid = CORRELATION_UID
        deviceType = DEVICE_TYPE
        organizationIdentification = ORGANIZATION_IDENTIFICATION
        domain = DOMAIN
        domainVersion = DOMAIN_VERSION
        priority = 1
    }
