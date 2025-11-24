package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.models.MikronikaDevicePublicKey
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service.MikronikaDeviceService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.signing.SigningService
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client.ClientSocket
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.DeviceRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests.GetStatusRequest
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.domain.DeviceResponseMessageMapper.toGetStatusResponse
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RequestType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.ResponseHeader
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.deviceResponseMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.responseHeader
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class DeviceRequestService(
    private val mikronikaDeviceService: MikronikaDeviceService,
    private val signingService: SigningService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger {}

    fun handleDeviceRequestMessage(requestMessage: DeviceRequestMessage) {
        val handler = when (requestMessage.header.requestType) {
            RequestType.GET_STATUS_REQUEST -> ::handleGetStatusRequest
            else -> TODO()
        }
        handler.invoke(requestMessage)
    }

    private fun handleGetStatusRequest(requestMessage: DeviceRequestMessage) {
        val deviceIdentification = requestMessage.header.deviceIdentification
        val getStatusRequest = GetStatusRequest(
            deviceIdentification,
            "", // TODO
        )

        val mikronikaDevice =
            mikronikaDeviceService.findBydeviceIdentification(deviceIdentification)

        sendClientMessage(mikronikaDevice, getStatusRequest) { responseEnvelope: Envelope ->
            val response = responseEnvelope.message.getStatusResponse.toGetStatusResponse()

            val message = deviceResponseMessage {
                header = buildHeader(mikronikaDevice)
                getStatusResponse = response
            }

            eventPublisher.publishEvent(response)
        }
    }

    private fun buildHeader(mikronikaDevice: MikronikaDevice): ResponseHeader = responseHeader {
        //TODO: Build response header
    }

    private fun sendClientMessage(
        device: MikronikaDevice,
        deviceRequest: DeviceRequest,
        responseMapper: (Envelope) -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val requestEnvelope = createEnvelope(device, deviceRequest.toOslpMessage())

            val sock = ClientSocket(deviceRequest.networkAddress, 12345)

            val responseEnvelope = sock.sendAndReceive(requestEnvelope)

            if (!validateSignature(responseEnvelope, MikronikaDevicePublicKey(device.publicKey))) {
                logger.error { "Signature validation failed for message! DeviceUid: ${responseEnvelope.deviceUid}" }
                return@launch
            }

            device.sequenceNumber = responseEnvelope.sequenceNumber

            responseMapper.invoke(responseEnvelope)

            mikronikaDeviceService.saveDevice(device)
        }
    }

    private fun validateSignature(
        responseEnvelope: Envelope,
        verificationMikronikaDevicePublicKey: MikronikaDevicePublicKey,
    ): Boolean = with(responseEnvelope) {
        signingService.verifySignature(
            sequenceNumber.toByteArray(2) + deviceUid + lengthIndicator.toByteArray(2) + messageBytes,
            securityKey,
            verificationMikronikaDevicePublicKey,
        )
    }

    private fun createEnvelope(device: MikronikaDevice, payload: Oslp.Message): Envelope {
        val deviceUidBytes = device.deviceUid?.toByteArray()
            ?: throw IllegalArgumentException("DeviceUid is empty for device ${device.deviceIdentification}")

        val sequenceNumber = device.sequenceNumber
            ?: throw IllegalArgumentException("sequenceNumber is empty for device ${device.deviceIdentification}")

        val lengthIndicator = payload.serializedSize
        val messageBytes = payload.toByteArray()

        val signature = signingService.createSignature(
            sequenceNumber.toByteArray(2)
                    + deviceUidBytes
                    + lengthIndicator.toByteArray(2)
                    + messageBytes
        )

        val envelope = Envelope(
            securityKey = signature,
            sequenceNumber = sequenceNumber,
            deviceUid = deviceUidBytes,
            lengthIndicator = lengthIndicator,
            messageBytes = messageBytes,
        )

        return envelope
    }
}
