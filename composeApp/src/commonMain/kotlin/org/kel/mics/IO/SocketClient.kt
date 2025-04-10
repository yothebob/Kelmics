package org.kel.mics.IO

import androidx.compose.runtime.MutableState
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalType

suspend expect fun createClientSocket(address: String, port: Int,  message: String) : String

suspend expect fun dispatchSocketCall(address: String, port: Int,  message: String, resVal: MutableState<MalString>) : MalType
