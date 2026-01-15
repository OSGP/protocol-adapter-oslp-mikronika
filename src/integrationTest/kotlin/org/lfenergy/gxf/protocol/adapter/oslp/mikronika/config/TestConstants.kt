// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config

object TestConstants {
    const val DEVICE_IDENTIFICATION_HEADER = "DeviceIdentification"
    const val DEVICE_IDENTIFICATION = "device_001"
    const val DEVICE_UID = "MIK--UID-001"
    const val SEQUENCE_NUMBER = 1
    const val RANDOM_DEVICE = 12345
    const val RANDOM_PLATFORM = 67890
    const val NETWORK_ADDRESS = "127.0.0.1"
    const val CORRELATION_UID = "19083029498y6asdljk"
    const val DEVICE_TYPE = "SSLD"
    const val ORGANIZATION_IDENTIFICATION = "LianderNetManagement"

    // Event Notification Test Constants
    const val EVENT_TYPE = "DIAG_EVENTS_GENERAL"
    const val EVENT_TIMESTAMP = "20251117101530"
    const val EVENT_DESCRIPTION = "Just a test event"

    const val DEVICE_EVENTS_QUEUE = "gxf.publiclighting.oslp-mikronika.device-events"

    const val DEVICE_REQUEST_QUEUE = "gxf.publiclighting.oslp-mikronika.device-requests"
    const val DEVICE_RESPONSE_QUEUE = "gxf.publiclighting.oslp-mikronika.device-responses"

    const val AUDIT_LOG_QUEUE = "gxf.publiclighting.oslp-mikronika.audit-log"
}
