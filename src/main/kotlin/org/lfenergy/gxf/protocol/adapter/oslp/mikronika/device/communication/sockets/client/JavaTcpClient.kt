// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.sockets.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.net.SocketTimeoutException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket

class JavaTcpClient(
    destination: InetSocketAddress,
    private val sslContext: SSLContext? = null,
    private val proxyConfig: InetSocketAddress? = null,
) : TcpClient(destination) {
    override suspend fun send(bytes: ByteArray): ByteArray =
        withContext(Dispatchers.IO) {
            val proxy =
                proxyConfig?.let {
                    Proxy(
                        Proxy.Type.HTTP,
                        it,
                    )
                } ?: Proxy.NO_PROXY

            Socket(proxy).use { rawSocket ->
                rawSocket.soTimeout = 2_000
                rawSocket.connect(destination, 5_000)

                if (sslContext != null) {
                    val sslSocket =
                        sslContext!!.socketFactory.createSocket(
                            rawSocket,
                            destination.hostName,
                            destination.port,
                            true,
                        ) as SSLSocket

                    sslSocket.enabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")
                    sslSocket.use { socket ->
                        socket.startHandshake()

                        socket.outputStream.write(bytes)
                        socket.outputStream.flush()

                        return@withContext readUntilEofOrIdle(socket.inputStream)
                    }
                } else {
                    rawSocket.outputStream.write(bytes)
                    rawSocket.outputStream.flush()

                    return@withContext readUntilEofOrIdle(rawSocket.inputStream)
                }
            }
        }

    private fun readUntilEofOrIdle(input: InputStream): ByteArray {
        val buffer = ByteArray(4096)
        val result = ByteArrayOutputStream()

        while (true) {
            val read =
                try {
                    input.read(buffer)
                } catch (_: SocketTimeoutException) {
                    break
                }

            if (read == -1) break
            if (read > 0) result.write(buffer, 0, read)
        }

        return result.toByteArray()
    }
}
