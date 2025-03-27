package org.kel.mics.IO

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


actual suspend fun createClientSocket(address: String, port: Int) {
    withContext(Dispatchers.IO) {
        try {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).tcp().connect(address, port)


            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel( autoFlush = true)
//        val thing = SocketInterface(sendChannel=sendChannel, socket=socket)

//        launch(Dispatchers.IO) {
//            while (true) {
//                val greeting = receiveChannel.readUTF8Line()
//                if (greeting != null) {
//                    println(greeting)
//                } else {
//                    println("Server closed a connection")
//                    socket.close()
//                    selectorManager.close()
//                    exitProcess(0)
//                }
//            }
//        }
        } catch (e: Exception) {
            println("CONNECTION FAILED")
            // Code for handling the exception
        }



    }
}

actual suspend fun sendSocketRequest(message: String): String {
    return ""
//    withContext(Dispatchers.IO) {
//        sendChannel.writeStringUtf8("$message\n")
//        val greeting = receiveChannel.readUTF8Line()
//        if (greeting != null) {
//            println(greeting)
//        } else {
//            println("Server closed a connection")
//            socket.close()
//            selectorManager.close()
//            exitProcess(0)
//        }
//    }
}