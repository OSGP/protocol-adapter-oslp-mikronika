// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import java.net.InetSocketAddress

abstract class TcpClient(
    val destination: InetSocketAddress,
) {
    abstract suspend fun send(bytes: ByteArray): ByteArray
}
