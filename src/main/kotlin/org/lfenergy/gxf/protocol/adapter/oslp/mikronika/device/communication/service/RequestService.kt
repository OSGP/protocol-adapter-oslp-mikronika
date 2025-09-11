// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

import com.google.protobuf.kotlin.toByteString
import com.google.protobuf.util.JsonFormat
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityNotFoundException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.InvalidJsonException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception.SendAndReceiveException
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.ByteArrayHelpers.Companion.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.ClientSocket
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaDeviceEntity
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.MikronikaRepository
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.Oslp.LightValue
import org.opensmartgridplatform.oslp.Oslp.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RequestService(private val signingService: SigningService, private val mikronikaRepository: MikronikaRepository) {
    private val logger = KotlinLogging.logger {}
    private val clientSocket = ClientSocket()

    fun getFirmwareVersion(deviceIdentification: String) {
        logger.debug { "Get firmware version for device: $deviceIdentification" }
        val payload =
            Message
                .newBuilder()
                .setGetFirmwareVersionRequest(Oslp.GetFirmwareVersionRequest.newBuilder().build())
                .build()

        sendAndReceiveRequest(payload, deviceIdentification)
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun startSelfTest(deviceIdentification: String) {
        val payload =
            Message.newBuilder().setStartSelfTestRequest(Oslp.StartSelfTestRequest.newBuilder().build()).build()

        GlobalScope.launch {
            sendAndReceiveRequest(payload, deviceIdentification)
//            TODO can this delay not be replaced by a callback when the self test is done?
            delay(10_000)
            stopSelfTest(deviceIdentification)
        }
    }

    private fun stopSelfTest(deviceIdentification: String) {
        val payload = Message.newBuilder().setStopSelfTestRequest(Oslp.StopSelfTestRequest.newBuilder().build()).build()

        sendAndReceiveRequest(payload, deviceIdentification)
    }

    fun startReboot(deviceIdentification: String) {
        val deviceStateService = DeviceStateService.getInstance()
        deviceStateService.resetRegistrationValues()

        val payload = Message.newBuilder().setSetRebootRequest(Oslp.SetRebootRequest.newBuilder().build()).build()

        sendAndReceiveRequest(payload, deviceIdentification)
    }

    fun setLightSensor(num: Int, deviceIdentification: String) {
        val payload =
            Message
                .newBuilder()
                .setSetTransitionRequest(Oslp.SetTransitionRequest.newBuilder().setTransitionTypeValue(num))
                .build()

        sendAndReceiveRequest(payload, deviceIdentification)
    }

    fun getStatus(deviceIdentification: String): String {
        val payload = Message.newBuilder().setGetStatusRequest(Oslp.GetStatusRequest.newBuilder().build()).build()

        sendAndReceiveRequest(payload,deviceIdentification)
    }

    fun getConfiguration(deviceIdentification: String) {
        val payload =
            Message.newBuilder().setGetConfigurationRequest(Oslp.GetConfigurationRequest.newBuilder().build()).build()

        sendAndReceiveRequest(payload, deviceIdentification)
    }

    fun setLightRequest(
        index: Int,
        on: Boolean,
        deviceIdentification: String,
    ) {
        val lightValue = LightValue.newBuilder()
        lightValue.setOn(on)
        lightValue.setIndex(index.toByteArray(1).toByteString())

        val payload =
            Message
                .newBuilder()
                .setSetLightRequest(
                    Oslp.SetLightRequest
                        .newBuilder()
                        .addValues(lightValue)
                        .build(),
                ).build()

        sendAndReceiveRequest(payload,deviceIdentification)
    }

//    TODO check if needed for the mikronika adapter
    fun sendJsonCommands(bytes: ByteArray, deviceIdentification: String) {
        runCatching {
            parseBytesToJsonArray(bytes).forEach {
                val message = Message.newBuilder()
                JsonFormat.parser().merge(it.jsonObject.toString(), message)
                sendAndReceiveRequest(message.build(), deviceIdentification)
            }
        }.onFailure { error -> logger.error { error.message ?: "Invalid JSON file" } }
    }

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

    private fun sendAndReceiveRequest(payload: Message, deviceIdentification: String) {
        val device: MikronikaDeviceEntity = mikronikaRepository.findByDeviceIdentification(deviceIdentification)?: throw EntityNotFoundException("Device with identification $deviceIdentification not found")

        val sequenceNumber = device.sequenceNumber ?: 0
        val deviceId = device.deviceIdentification.toByteArray()
        val lengthIndicator = payload.serializedSize
        val messageBytes = payload.toByteArray()

        val deviceAddress = device.deviceAddress ?: throw SendAndReceiveException("Device address is not set for device $deviceIdentification")
        val devicePort = device.devicePort ?: throw SendAndReceiveException("Device port is not set for device $deviceIdentification")

        val signature =
            signingService.createSignature(
                    sequenceNumber.toByteArray(2) + deviceId + lengthIndicator.toByteArray(2) + messageBytes,
                )

        val request = Envelope(signature, sequenceNumber, deviceId, lengthIndicator, messageBytes)

        clientSocket.sendAndReceive(request, deviceAddress, devicePort)
    }
}
