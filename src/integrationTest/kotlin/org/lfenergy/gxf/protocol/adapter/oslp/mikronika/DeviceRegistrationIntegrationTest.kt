// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika

import com.google.protobuf.ByteString
import com.gxf.utilities.oslp.message.signing.SigningUtil
import jakarta.jms.BytesMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.ContainerConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.SecurityConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.config.encodedAsBase64
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.domain.Envelope
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.configuration.ServerSocketConfiguration
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.adapter.MikronikaDevice
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.database.core.CoreDevice
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.DeviceEventMessage
import org.lfenergy.gxf.publiclighting.contracts.internal.device_events.EventType
import org.opensmartgridplatform.oslp.Oslp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.test.context.ContextConfiguration
import java.net.Socket
import java.security.KeyPair


@SpringBootTest()
@EnableAsync
@ContextConfiguration(classes = [ContainerConfiguration::class, SecurityConfiguration::class])
class DeviceRegistrationIntegrationTest {
    @Autowired
    private lateinit var adapterJdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var coreJdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var deviceKeyPair: KeyPair

    @Autowired
    private lateinit var signingUtil: SigningUtil

    @Autowired
    private lateinit var serverSocketConfiguration: ServerSocketConfiguration

    private var mikronikaDevice: MikronikaDevice? = null
    private var coreDevice: CoreDevice? = null
    private var requestEnvelope: Envelope? = null
    private var responseEnvelope: Envelope? = null

    @BeforeEach
    fun setup() {
        jmsTemplate.receiveTimeout = 2000

        val base64PublicKey = deviceKeyPair.public.encodedAsBase64()
        adapterJdbcTemplate.execute("update oslp_mikronika_device set public_key = '$base64PublicKey'")

        mikronikaDevice = null
        coreDevice = null
        responseEnvelope = null
    }

    @Test
    fun testDeviceRegistration() {
        `given an existing device`()
        `when the device sends a device registration request message`()
        `then a device registration event message should be sent to the message broker`()
        `then a device registration response message should be sent back to the device`()
        `then the device should be updated in the database`()
    }

    private fun `given an existing device`() {
        mikronikaDevice =
            adapterJdbcTemplate.queryForObject(SELECT_ADAPTER_DEVICE_SQL, ::mapAdapterDevice, DEVICE_IDENTIFICATION)
        coreDevice = coreJdbcTemplate.queryForObject(SELECT_CORE_DEVICE_SQL, ::mapCoreDevice, DEVICE_IDENTIFICATION)
    }

    private fun `when the device sends a device registration request message`() {
        requestEnvelope = requestEnvelope()
        Socket(serverSocketConfiguration.hostName, serverSocketConfiguration.port).use { socket ->
            socket.getOutputStream().write(requestEnvelope!!.getBytes())
            responseEnvelope = Envelope.parseFrom(socket.getInputStream().readBytes())
        }
    }

    private fun `then a device registration response message should be sent back to the device`() {
        assertThat(responseEnvelope?.message).isNotNull
        assertThat(responseEnvelope!!.message.hasRegisterDeviceResponse()).isTrue

        with(responseEnvelope!!.message.registerDeviceResponse) {
            assertThat(this).isNotNull()
            assertThat(status).isEqualTo(Oslp.Status.OK)
            assertThat(randomDevice).isEqualTo(RANDOM_DEVICE)
            assertThat(randomPlatform).isNotEqualTo(0)
        }

        with(responseEnvelope!!.message.registerDeviceResponse.locationInfo) {
            assertThat(this).isNotNull()
            assertThat(latitude).isEqualTo((coreDevice!!.latitude * GPS_SCALE_FACTOR).toInt())
            assertThat(longitude).isEqualTo((coreDevice!!.longitude * GPS_SCALE_FACTOR).toInt())
        }
    }

    private fun `then a device registration event message should be sent to the message broker`() {
        val bytesMessage = jmsTemplate.receive("gxf.publiclighting.oslp-mikronika.device-events") as BytesMessage?

        assertThat(bytesMessage).isNotNull
        assertThat(bytesMessage!!.jmsType).isEqualTo(EventType.DEVICE_REGISTRATION.name)
        assertThat(bytesMessage.getStringProperty("DeviceIdentification")).isEqualTo(DEVICE_IDENTIFICATION)

        val eventMessage = bytesMessage.toDeviceEventMessage()
        with(eventMessage.header) {
            assertThat(this).isNotNull
            assertThat(deviceIdentification).isEqualTo(DEVICE_IDENTIFICATION)
            assertThat(eventType).isEqualTo(EventType.DEVICE_REGISTRATION)
            assertThat(deviceType).isEqualTo("SSLD")
        }
        with(eventMessage.deviceRegistrationReceivedEvent) {
            assertThat(this).isNotNull
            assertThat(networkAddress).isEqualTo("127.0.0.1")
            assertThat(hasSchedule).isTrue
        }

    }

    private fun BytesMessage.toDeviceEventMessage(): DeviceEventMessage {
        val bytes = ByteArray(this.bodyLength.toInt())
        this.readBytes(bytes)
        return DeviceEventMessage.parseFrom(bytes)
    }

    private fun `then the device should be updated in the database`() {
        val updatedDevice =
            adapterJdbcTemplate.queryForObject(
                SELECT_ADAPTER_DEVICE_SQL,
                ::mapAdapterDevice,
                DEVICE_IDENTIFICATION,
            )!!
        assertThat(updatedDevice.randomPlatform).isNotEqualTo(0)
        assertThat(updatedDevice.randomDevice).isEqualTo(RANDOM_DEVICE)
    }

    private fun deviceRegistrationRequestMessage() =
        Oslp.Message
            .newBuilder()
            .setRegisterDeviceRequest(
                Oslp.RegisterDeviceRequest
                    .newBuilder()
                    .setDeviceIdentification(DEVICE_IDENTIFICATION)
                    .setIpAddress(ByteString.copyFrom(byteArrayOf(127, 0, 0, 1)))
                    .setHasSchedule(true)
                    .setRandomDevice(RANDOM_DEVICE)
                    .setDeviceType(Oslp.DeviceType.SSLD),
            ).build()

    private fun requestEnvelope(): Envelope {
        val payload = deviceRegistrationRequestMessage().toByteArray()
        val sequenceNumber = SEQUENCE_NUMBER
        val deviceUid = DEVICE_UID
        val byteArray =
            sequenceNumber.toByteArray(2) +
                    deviceUid.toByteArray() +
                    payload.size.toByteArray(2) +
                    payload

        val signature = signingUtil.createSignature(byteArray, deviceKeyPair.private)

        return Envelope(
            sequenceNumber = sequenceNumber,
            deviceUid = deviceUid.toByteArray(),
            lengthIndicator = payload.size,
            messageBytes = payload,
            securityKey = signature,
        )
    }

    private fun mapAdapterDevice(rs: java.sql.ResultSet, @Suppress("UNUSED_PARAMETER") rowNum: Int) =
        MikronikaDevice(
            deviceIdentification = rs.getString("device_identification"),
            randomDevice = rs.getInt("random_device"),
            randomPlatform = rs.getInt("random_platform"),
        )

    private fun mapCoreDevice(rs: java.sql.ResultSet, @Suppress("UNUSED_PARAMETER") rowNum: Int) =
        CoreDevice(
            deviceIdentification = rs.getString("device_identification"),
            latitude = rs.getFloat("gps_latitude"),
            longitude = rs.getFloat("gps_longitude"),
        )

    companion object {
        private const val DEVICE_IDENTIFICATION = "device_001"
        private const val DEVICE_UID = "MIK--UID-001"
        private const val SEQUENCE_NUMBER = 1
        private const val RANDOM_DEVICE = 123

        private const val GPS_SCALE_FACTOR = 1E6F

        private const val SELECT_ADAPTER_DEVICE_SQL =
            "select device_identification, random_device, random_platform from oslp_mikronika_device where device_identification = ?"
        private const val SELECT_CORE_DEVICE_SQL =
            "select device_identification, gps_latitude, gps_longitude from device where device_identification = ?"
    }
}
