package org.kel.mics.IO

suspend expect fun createClientSocket(address: String, port: Int,  message: String) : String
