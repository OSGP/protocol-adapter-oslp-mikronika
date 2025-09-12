// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidJsonException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.ClientSocket
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaRepository
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.stereotype.Service

@Service
class RequestService(
    private val signingService: SigningService,
    private val mikronikaRepository: MikronikaRepository,
) {
    private val logger = KotlinLogging.logger {}
    private val clientSocket = ClientSocket()

    private fun parseBytesToJsonArray(bytes: ByteArray): JsonArray {
        val jsonString = bytes.toString(Charsets.UTF_8)
        try {
            val root = Json.parseToJsonElement(jsonString).jsonObject
            val requests = root["requests"]?.jsonArray
            validateJsonRequest(requests)
            return requests!!
        } catch (_: IllegalArgumentException) {
            throw InvalidJsonException("Invalid JSON file")
        }
    }

    private fun validateJsonRequest(jsonArray: JsonArray?) {
        jsonArray ?: throw InvalidJsonException("Missing requests object list")
        if (jsonArray.isEmpty()) throw InvalidJsonException("No commands found in the json file")
    }

    private fun sendAndReceiveRequest(
        payload: Message,
        deviceIdentification: String,
    ) {
//        val device: MikronikaDeviceEntity =
//            mikronikaRepository.findByDeviceIdentification(
//                deviceIdentification,
//            ) ?: throw EntityNotFoundException("Device with identification $deviceIdentification not found")
//
//        val sequenceNumber = device.sequenceNumber ?: 0
//        val deviceId = device.deviceIdentification.toByteArray()
//        val lengthIndicator = payload.serializedSize
//        val messageBytes = payload.toByteArray()
//
//        val deviceAddress =
//            device.deviceAddress ?: throw SendAndReceiveException("Device address is not set for device $deviceIdentification")
//        val devicePort = device.devicePort ?: throw SendAndReceiveException("Device port is not set for device $deviceIdentification")
//
//        val signature =
//            signingService.createSignature(
//                sequenceNumber.toByteArray(2) + deviceId + lengthIndicator.toByteArray(2) + messageBytes,
//            )
//
//        val request = Envelope(signature, sequenceNumber, deviceId, lengthIndicator, messageBytes)
//
//        clientSocket.sendAndReceive(request, deviceAddress, devicePort)
    }
}
