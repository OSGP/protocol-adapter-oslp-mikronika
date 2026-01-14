// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.kotlin.toByteString
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
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
    device: Device,
    organization: Organization,
    val scheduleEntries: List<ScheduleEntry>,
    val pageInfo: PageInfo,
) : DeviceRequest(
        device,
        organization,
    ) {
    override fun toOslpMessage(): Oslp.Message =
        message {
            setScheduleRequest =
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
            }
        }

    private fun List<LightValue>.toOslpLightValues() =
        map { entry ->
            lightValue {
                index =
                    entry.index.number
                        .toByteArray(1)
                        .toByteString()
                on = entry.lightOn
            }
        }
}
