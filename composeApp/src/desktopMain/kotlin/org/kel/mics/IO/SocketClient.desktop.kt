package org.kel.mics.IO

import androidx.compose.runtime.mutableStateOf
import arrow.core.Either
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
): Either<MalType, MalType> {
    withContext(Dispatchers.IO) {
        try {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).tcp().connect(address, port)
            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel( autoFlush = true)
            if (!message.isNullOrEmpty()) {
                sendChannel.writeStringUtf8("$message\n")
                socket.close()
                println("DisCOnnecting...")
            }
            return@withContext MalConstant("true").right()
        } catch (e: Exception) {
            println("CONNECTION FAILED")
            return@withContext NIL.left()
        }
    }
    return MalConstant("true").right()
}