// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

class DeviceStateService {
    private var deviceRegistered = false
    private var deviceRegistrationConfirmed = false

    var deviceId: ByteArray = byteArrayOf()
    var sequenceNumber: Int = 0
    var randomPlatform: Int = 0
    var randomDevice: Int = 0

    fun registerDevice(deviceId: ByteArray) {
        this.deviceId = deviceId
        deviceRegistered = true
    }

    fun updateSequenceNumber(newSequenceNumber: Int) {
        sequenceNumber = newSequenceNumber
    }

    fun confirmRegisterDevice(newSequenceNumber: Int) {
        deviceRegistrationConfirmed = true
        sequenceNumber = newSequenceNumber
    }

    fun resetRegistrationValues() {
        deviceRegistered = false
        deviceRegistrationConfirmed = false
    }

    companion object {
        private var instance: DeviceStateService? = null

        fun createInstance() {
            instance = DeviceStateService()
        }

        fun getInstance(): DeviceStateService = instance ?: throw IllegalStateException("No DeviceStateService instance found!")
    }
}
