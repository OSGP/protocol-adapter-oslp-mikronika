package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.DeviceResponseMessage
import org.opensmartgridplatform.oslp.Oslp

abstract class DeviceRequest(
    val deviceIdentification: String,
    val networkAddress: String,
) {
    abstract fun toOslpMessage(): Oslp.Message
}
