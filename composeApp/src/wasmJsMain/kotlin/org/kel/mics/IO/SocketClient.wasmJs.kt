package org.kel.mics.IO

import org.kel.mics.Mal.MalType

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
): MalType {
    TODO("Not yet implemented")
}