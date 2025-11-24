package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.devicerequest.domain

import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.GetStatusResponse
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.getStatusResponse
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.lightValue
import org.opensmartgridplatform.oslp.Oslp
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LightType as InternalLightType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LightValue as InternalLightValue
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.LinkType as InternalLinkType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.Result as InternalResult

object DeviceResponseMessageMapper {

    fun Oslp.GetStatusResponse.toGetStatusResponse(): GetStatusResponse = getStatusResponse {
        result = when (status) {
            Oslp.Status.OK -> InternalResult.OK
            else -> InternalResult.NOT_OK
        }

        lightValues += valueList.map { it.toInternal() }

        preferredLinkType = this@toGetStatusResponse.preferredLinktype.toInternal()

        actualLinkType = this@toGetStatusResponse.actualLinktype.toInternal()

        lightType = when (this@toGetStatusResponse.lightType) {
            Oslp.LightType.RELAY -> InternalLightType.RELAY
            Oslp.LightType.ONE_TO_TEN_VOLT -> InternalLightType.ONE_TO_TEN_VOLT
            Oslp.LightType.ONE_TO_TEN_VOLT_REVERSE -> InternalLightType.ONE_TO_TEN_VOLT_REVERSE
            Oslp.LightType.DALI -> InternalLightType.DALI
            else -> InternalLightType.RELAY
        }
        eventNotificationMask = eventNotificationMask
        numberOfOutputs = numberOfOutputs
        dcOutputVoltageMaximum = dcOutputVoltageMaximum
        dcOutputVoltageCurrent = dcOutputVoltageCurrent
        maximumOutputPowerOnDcOutput = maximumOutputPowerOnDcOutput
        serialNumber = serialNumber
        macAddress = macAddress
        hardwareId = hardwareId
        internalFlashMemSize = internalFlashMemSize
        externalFlashMemSize = externalFlashMemSize
        lastInternalTestResultCode = lastInternalTestResultCode
        startupCounter = startupCounter
        bootLoaderVersion = bootLoaderVersion
        firmwareVersion = firmwareVersion
        currentConfigurationBackUsed = currentConfigurationBackUsed
        name = name
        currentTime = currentTime
        currentIp = currentIp
    }

    private fun Oslp.LinkType.toInternal(): InternalLinkType = when (this) {
        Oslp.LinkType.GPRS -> InternalLinkType.GPRS
        Oslp.LinkType.CDMA -> InternalLinkType.CDMA
        Oslp.LinkType.ETHERNET -> InternalLinkType.ETHERNET
        else -> InternalLinkType.GPRS // Default?
    }

    private fun Oslp.LightValue.toInternal(): InternalLightValue = lightValue {
        index = when (index.number) {
            0 -> org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayIndex.RELAY_ALL
            1 -> org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayIndex.RELAY_ONE
            2 -> org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayIndex.RELAY_TWO
            3 -> org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayIndex.RELAY_THREE
            4 -> org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayIndex.RELAY_FOUR
            else -> org.lfenergy.gxf.publiclighting.contracts.internal.device_responses.RelayIndex.RELAY_ALL
        }
        lightOn = on
    }
}
