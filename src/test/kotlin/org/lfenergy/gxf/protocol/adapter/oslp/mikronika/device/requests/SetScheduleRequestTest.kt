// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.requests

import com.google.protobuf.ByteString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.DEVICE_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.TestObjects.NETWORK_ADDRESS
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.command.mapper.ORGANIZATION_IDENTIFICATION
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.helpers.toByteArray
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Device
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.domain.Organization
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.ActionTime
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.RelayIndex
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.TriggerType
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.Weekday
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.lightValue
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.scheduleEntry
import org.lfenergy.gxf.publiclighting.contracts.internal.device_requests.triggerWindow
import org.opensmartgridplatform.oslp.Oslp

class SetScheduleRequestTest {
    @Test
    fun `should map correctly with given schedules`() {
        val subject =
            SetScheduleRequest(
                Device(DEVICE_IDENTIFICATION, NETWORK_ADDRESS),
                Organization(ORGANIZATION_IDENTIFICATION),
                listOf(
                    scheduleEntry {
                        weekday = Weekday.MONDAY
                        startDay = "20220913"
                        endDay = "20240913"
                        actionTime = ActionTime.SUNRISE_TIME
                        time = "120000"
                        window =
                            triggerWindow {
                                minutesBefore = 10
                                minutesAfter = 20
                            }
                        value.addAll(
                            listOf(
                                lightValue {
                                    index = RelayIndex.RELAY_ONE
                                    lightOn = true
                                },
                                lightValue {
                                    index = RelayIndex.RELAY_TWO
                                    lightOn = false
                                },
                            ),
                        )
                        triggerType = TriggerType.LIGHT_TRANSITION
                        minimumLightsOn = 30
                        index = 3
                        isEnabled = true
                    },
                ),
                SetScheduleRequest.PageInfo(1, 2, 3),
            )

        val result = subject.toOslpMessage()

        assertThat(result.hasSetScheduleRequest()).isTrue
        with(result.setScheduleRequest) {
            assertThat(pageInfo.currentPage).isEqualTo(1)
            assertThat(pageInfo.pageSize).isEqualTo(2)
            assertThat(pageInfo.totalPages).isEqualTo(3)
            assertThat(scheduleType).isEqualTo(Oslp.RelayType.LIGHT)

            assertThat(schedulesList).hasSize(1)
            with(schedulesList[0]) {
                assertThat(weekday).isEqualTo(Oslp.Weekday.MONDAY)
                assertThat(startDay).isEqualTo("20220913")
                assertThat(endDay).isEqualTo("20240913")
                assertThat(actionTime).isEqualTo(Oslp.ActionTime.SUNRISE)
                assertThat(time).isEqualTo("120000")
                assertThat(window.minutesBefore).isEqualTo(10)
                assertThat(window.minutesAfter).isEqualTo(20)
                assertThat(valueList).hasSize(2)
                with(valueList[0]) {
                    assertThat(index).isEqualTo(ByteString.copyFrom(1.toByteArray(1)))
                    assertThat(on).isTrue
                }
                with(valueList[1]) {
                    assertThat(index).isEqualTo(ByteString.copyFrom(2.toByteArray(1)))
                    assertThat(on).isFalse
                }
            }
        }
    }

    @Test
    fun `should map correctly with empty schedule`() {
        val subject =
            SetScheduleRequest(
                Device(DEVICE_IDENTIFICATION, NETWORK_ADDRESS),
                Organization(ORGANIZATION_IDENTIFICATION),
                listOf(
                    scheduleEntry {
                        value.addAll(
                            listOf(
                                lightValue {
                                },
                            ),
                        )
                    },
                ),
                SetScheduleRequest.PageInfo(1, 2, 3),
            )

        val result = subject.toOslpMessage()

        assertThat(result.hasSetScheduleRequest()).isTrue
        with(result.setScheduleRequest) {
            assertThat(schedulesList).hasSize(1)
            with(schedulesList[0]) {
                assertThat(hasWeekday()).isTrue
                assertThat(hasActionTime()).isTrue

                assertThat(hasStartDay()).isFalse
                assertThat(hasEndDay()).isFalse
                assertThat(hasTime()).isFalse
                assertThat(hasWindow()).isFalse
                assertThat(valueList).hasSize(1)
                with(valueList[0]) {
                    assertThat(hasIndex()).isTrue
                    assertThat(hasOn()).isTrue
                }
            }
        }
    }
}
