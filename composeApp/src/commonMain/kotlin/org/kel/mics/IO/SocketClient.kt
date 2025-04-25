package org.kel.mics.IO

import androidx.compose.runtime.snapshots.SnapshotStateList
import org.kel.mics.Mal.MalString
import org.kel.mics.Mal.MalType

fun translateNewLines(str: String): String {
    return str.replace("<RNL>", "\r\n").replace("<NL>", "\n")
}

suspend expect fun createClientSocket(address: String, port: Int,  message: String) : String

suspend expect fun dispatchSocketCall(address: String, port: Int, message: String, resVal: SnapshotStateList<MalString>) : MalType
