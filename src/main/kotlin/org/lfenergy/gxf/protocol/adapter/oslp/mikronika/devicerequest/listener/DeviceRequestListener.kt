package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.listener

import com.google.protobuf.InvalidProtocolBufferException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.jms.BytesMessage
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.ApplicationConstants.JMS_PROPERTY_DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.service.DeviceRequestService
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.DeviceRequestMessage
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
class DeviceRequestListener(
    private val deviceRequestService: DeviceRequestService,
) {
    private val logger = KotlinLogging.logger {}

    @JmsListener(destination = $$"${device-commands.consumer.inbound-queue}")
    fun onMessage(bytesMessage: BytesMessage) {
        val correlationId = bytesMessage.jmsCorrelationID
        val deviceId = bytesMessage.getStringProperty(JMS_PROPERTY_DEVICE_IDENTIFICATION)
        val messageType = bytesMessage.jmsType

        logger.info { "Received event for device $deviceId of type $messageType with correlation uid $correlationId." }

        try {
            val deviceRequestMessage = bytesMessage.parseToDeviceRequestMessage()
            deviceRequestService.handleDeviceRequestMessage(deviceRequestMessage)
        } catch (e: InvalidProtocolBufferException) {
            logger.error(e) {
                "Received invalid protocol buffer message with correlation uid $correlationId."
            }
        } catch (e: IllegalArgumentException) {
            logger.error(e) {
                "Received invalid event for device $deviceId in message with correlation uid $correlationId."
            }
        }
    }

    private fun BytesMessage.parseToDeviceRequestMessage(): DeviceRequestMessage {
        val bytes = ByteArray(this.bodyLength.toInt())
        this.readBytes(bytes)
        return DeviceRequestMessage.parseFrom(bytes)
    }

}
