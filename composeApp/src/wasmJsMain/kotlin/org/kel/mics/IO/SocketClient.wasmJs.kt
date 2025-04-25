package org.kel.mics.IO

import androidx.compose.runtime.snapshots.SnapshotStateList
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.kel.mics.ASYNC_BUFFER
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

@OptIn(InternalAPI::class)
actual suspend fun dispatchSocketCall(
    address: String,
    port: Int,
    message: String,
    resVal: SnapshotStateList<MalString>
): MalType {

//    val url = "ws://$address:$port"

    // Suspend until the socket is open

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



    val url = "http://$address:$port"

    val client = HttpClient() // You can use another engine depending on your target

        println("Sending via HTTP: $message")

    withContext(Dispatchers.Main)  {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Text.Plain)
                // I think I will need to adjust the server, but also maybe wrap the command in something like a <payload> xml tag
                setBody("PAYLOAD:${message}")
            }
            println("Response status: ${response.status}")
            println("Response body: ${response.bodyAsText()}")
        resVal.add(MalString(response.bodyAsText() ?: "(null)"))
        ASYNC_BUFFER.buf.writeUtf8(resVal.last().value)
        }

    return resVal.last()

//    } catch (e: Exception) {
//        println("HTTP request error: ${e.message}")
//    } finally {
//        client.close()
//    }
//    return NIL


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
