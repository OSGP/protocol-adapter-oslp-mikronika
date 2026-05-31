// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

class TcpClientFactory(
    val configuration: TcpClientConfigurationBuilder.() -> Unit,
) {
    fun createTcpClient(
        destinationHost: String,
        port: Int,
    ): JavaTcpClient =
        JavaTcpClient(destinationHost, port) {
            configuration()
        }
}
