// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.LightValue
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.ScheduleEntry
import org.opensmartgridplatform.oslp.Oslp
import org.opensmartgridplatform.oslp.lightValue
import org.opensmartgridplatform.oslp.message
import org.opensmartgridplatform.oslp.pageInfo
import org.opensmartgridplatform.oslp.schedule
import org.opensmartgridplatform.oslp.setScheduleRequest
import org.opensmartgridplatform.oslp.window

class SetScheduleRequest(
    deviceIdentification: String,
    networkAddress: String,
    private val scheduleEntries: List<ScheduleEntry>,
    private val pageInfo: PageInfo,
) : DeviceRequest(
        deviceIdentification,
        networkAddress,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setScheduleRequest {
                schedules.addAll(scheduleEntries.toSchedules())
                pageInfo =
                    pageInfo {
                        currentPage = this@SetScheduleRequest.pageInfo.currentPage
                        pageSize = this@SetScheduleRequest.pageInfo.pageSize
                        totalPages = this@SetScheduleRequest.pageInfo.totalPages
                    }
                scheduleType = Oslp.RelayType.LIGHT
            }
        }

    data class PageInfo(
        val currentPage: Int,
        val pageSize: Int,
        val totalPages: Int,
    )

    private fun List<ScheduleEntry>.toSchedules() =
        map { entry ->
            schedule {
                weekday = Oslp.Weekday.forNumber(entry.weekday.number)
                startDay = entry.startDay
                endDay = entry.endDay
                actionTime = Oslp.ActionTime.forNumber(entry.actionTime.number)
                time = entry.time

                if (entry.hasWindow()) {
                    window =
                        window {
                            minutesBefore = entry.window.minutesBefore
                            minutesAfter = entry.window.minutesAfter
                        }
                }

                value.addAll(entry.valueList.toOslpLightValues())

                triggerType = Oslp.TriggerType.forNumber(entry.triggerType.number)
                minimumLightsOn = entry.minimumLightsOn
                index = entry.index
                isEnabled = true // TODO should the be default?
            }
        }

    private fun List<LightValue>.toOslpLightValues() =
        map { entry ->
            lightValue {
                index // TODO: What should this be? bytes in oslp <> enum in internal
                on = entry.lightOn
                dimValue // TODO: Same as the above, bytes as type is impossible to use
            }
        }
}
