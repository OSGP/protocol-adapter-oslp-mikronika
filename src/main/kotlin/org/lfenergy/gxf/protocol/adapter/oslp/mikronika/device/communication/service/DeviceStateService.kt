// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.service

// TODO add DeviceStateService for multiple devices
class DeviceStateService {
    var deviceUid: ByteArray = byteArrayOf()
    var sequenceNumber: Int = 0
    var randomPlatform: Int = 0
    var randomDevice: Int = 0

    fun registerDevice(deviceUid: ByteArray) {
        this.deviceUid = deviceUid
    }

    fun updateSequenceNumber(newSequenceNumber: Int) {
        sequenceNumber = newSequenceNumber
    }

    fun confirmRegisterDevice(newSequenceNumber: Int) {
        sequenceNumber = newSequenceNumber
    }

    companion object {
        private var instance: DeviceStateService? = null

        fun createInstance(): DeviceStateService {
            instance = DeviceStateService()
            return instance!!
        }

        fun getInstance(): DeviceStateService = instance ?: createInstance()
    }
}
