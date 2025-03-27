package org.kel.mics.IO

import io.ktor.network.sockets.SocketBuilder
import io.ktor.utils.io.ByteWriteChannel

interface SocketInterface {
    val socket: SocketBuilder
    val sendChannel: ByteWriteChannel
}


suspend expect fun createClientSocket(address: String, port: Int)

suspend expect fun sendSocketRequest(message: String) : String