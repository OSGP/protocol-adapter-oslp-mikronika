// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika

import com.google.protobuf.timestamp
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.CORRELATION_UID
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.DEVICE_TYPE
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.NETWORK_ADDRESS
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.TestConstants.ORGANIZATION_IDENTIFICATION
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.requestHeader

fun createHeader(reqType: RequestType) =
    requestHeader {
        correlationUid = CORRELATION_UID
        deviceIdentification = DEVICE_IDENTIFICATION
        deviceType = DEVICE_TYPE
        organizationIdentification = ORGANIZATION_IDENTIFICATION
        requestType = reqType
        timestamp = timestamp {}
        networkAddress = NETWORK_ADDRESS
        domain = ""
        domainVersion = ""
        priority = 1
    }

const val FIRMWARE_VERSION = "V01"
