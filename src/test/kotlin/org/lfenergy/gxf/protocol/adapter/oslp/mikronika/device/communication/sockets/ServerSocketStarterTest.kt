// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.configuration.ServerSocketConfiguration

class ServerSocketStarterTest {
    @MockK(relaxed = true)
    private lateinit var serverSocket: ServerSocket

    @MockK
    private lateinit var serverSocketConfiguration: ServerSocketConfiguration

    @InjectMockKs
    private lateinit var starter: ServerSocketInitializer

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { serverSocketConfiguration.hostName } returns "localhost"
        every { serverSocketConfiguration.port } returns 1234
    }

    @Test
    fun `should call startListening on serverSocket with correct arguments`() {
        starter.start()
        verify(exactly = 1) { serverSocket.startListening("localhost", 1234) }
    }
}
