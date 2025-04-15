package org.kel.mics.IO

import androidx.compose.runtime.snapshots.SnapshotStateList
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalType
import org.kel.mics.Mal.NIL

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

    val url = "http://$address:$port"

    val client = HttpClient() // You can use another engine depending on your target

    try {
        println("Sending via HTTP: $message")

        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Text.Plain)
            // I think I will need to adjust the server, but also maybe wrap the command in something like a <payload> xml tag
            setBody("PAYLOAD:${message}")
        }

        println("Response status: ${response.status}")
        println("Response body: ${response.bodyAsText()}")
    } catch (e: Exception) {
        println("HTTP request error: ${e.message}")
    } finally {
        client.close()
    }
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
