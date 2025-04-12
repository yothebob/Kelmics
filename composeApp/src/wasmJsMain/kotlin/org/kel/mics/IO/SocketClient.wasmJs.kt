package org.kel.mics.IO

import androidx.compose.runtime.snapshots.SnapshotStateList
import io.ktor.client.fetch.RequestInit
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalType
import org.kel.mics.Mal.NIL
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual suspend fun createClientSocket(address: String, port: Int, message: String): String {
//    var res = mutableStateOf("")
//    withContext(Dispatchers.Default) {
//        try {
//
//            val selectorManager = SelectorManager(Dispatchers.IO)
//            val socket = aSocket(selectorManager).tcp().connect(address, port)
//            val receiveChannel = socket.openReadChannel()
//            val sendChannel = socket.openWriteChannel( autoFlush = true)
//            if (!message.isNullOrEmpty()) {
//                sendChannel.writeStringUtf8("$message\n")
//                res.value = receiveChannel.readUTF8Line().toString()
//                socket.close()
//                println("DisCOnnecting...")
//            }
//
//        } catch (e: Exception) {
//            println("CONNECTION FAILED")
//        }
//    }
//    return res.value?: ""
    return ""
}

actual suspend fun dispatchSocketCall(
    address: String,
    port: Int,
    message: String,
    resVal: SnapshotStateList<MalString>
): MalType {
//    return NIL
//        try {
            // Connect using ws://
            val url = "http://$address:$port"
            val socket = WebSocket(url)

            // Suspend until the socket is open
            suspendCoroutine<Unit> { cont ->
                socket.onopen = {
                    println("WebSocket connected to $url")
                    cont.resume(Unit)
                }
                socket.onerror = {
                    cont.resumeWithException(Exception("WebSocket connection error"))
                }
            }
//return NIL
//            // Send message
            println("Sending via WebSocket: $message")
            socket.send(message)

//            // Wait for a single response message

    // \/ THIS IS BROKEN BELOW
//            withTimeout(5000) {
//                suspendCoroutine<String> { cont ->
//                    socket.onmessage = { event ->
//                        println("asdasd")
//                        val data = (event as MessageEvent).data as String
//                        cont.resume(data)
//                    }
//                    socket.onerror = {
//                        cont.resumeWithException(Exception("WebSocket error during receive"))
//                    }
//                }
//            }
    return NIL


//
//            println("Received: $response")
//
//            val processed = response.replace("<RNL>", "\r\n").replace("<NL>", "\n")
//            resVal.add(MalString(processed))
//            return resVal.last()
//        } catch (e: Exception) {
//            println("ERROR: ${e.message}")
//            return NIL
//        }
}
