package org.kel.mics.IO

import androidx.compose.runtime.mutableStateOf
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

// Still a work in progress.. this is a socket connection to a shell aka bind shell..
// on server you can run a bind-shell process (some python socket listener)
// this only TECHNICALLY WORKS so far


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
