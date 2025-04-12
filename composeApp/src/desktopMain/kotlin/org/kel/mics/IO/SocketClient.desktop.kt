package org.kel.mics.IO

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import arrow.core.left
import arrow.core.right
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kel.mics.Mal.MalConstant
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalType
import org.kel.mics.Mal.NIL

actual suspend fun createClientSocket(address: String, port: Int, message: String): String {
    var res = mutableStateOf("")
    withContext(Dispatchers.IO) {
//        try {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect(address, port)
        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel( autoFlush = true)
        if (!message.isNullOrEmpty()) {
            sendChannel.writeStringUtf8("$message\n")
            res.value = receiveChannel.readUTF8Line().toString()
            socket.close()
            println("DisCOnnecting...")
        }

//        } catch (e: Exception) {
//            println("CONNECTION FAILED")
//        }
    }
    return res.value?: ""
}

actual suspend fun dispatchSocketCall(
    address: String,
    port: Int,
    message: String,
    resVal: SnapshotStateList<MalString>
): MalType {
    return try {
        withContext(Dispatchers.IO) {
            println("Connecting to $address:$port")
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).tcp().connect(address, port)

            println("Socket connected.")
            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            println("Sending: $message")
            sendChannel.writeStringUtf8("$message\n")
            sendChannel.flush()
            println("Message sent. Waiting for response...")

            val response = receiveChannel.readUTF8Line()
            println("Received: $response")

            withContext(Dispatchers.Main) {
                resVal.add(MalString(response!!.replace("<RNL>", "\r\n").replace("<NL>", "\n") ?: "(null)"))
            }
            resVal.last()
        }
    } catch (e: Exception) {
        println("ERROR: ${e.message}")
        NIL
    }
}